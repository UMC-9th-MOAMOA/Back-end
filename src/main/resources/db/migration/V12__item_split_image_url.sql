/* =========================================================
   V12 - item.image_url -> (shop_image_url, wearing_image_url) 분리
   - 1~46: shop(원래 이미지)
   - 101~146: wearing(실제 착용 이미지)
   - 최종: 1~46만 남기고, 101~146은 제거
   ========================================================= */

-- 1) 새 컬럼 추가 (일단 NULL 허용)
ALTER TABLE item
    ADD COLUMN shop_image_url VARCHAR(1000) NULL AFTER name,
    ADD COLUMN wearing_image_url VARCHAR(1000) NULL AFTER shop_image_url;

-- 2) shop_image_url = base(1~46)의 기존 image_url 복사
UPDATE item
SET shop_image_url = image_url
WHERE id BETWEEN 1 AND 46
  AND shop_image_url IS NULL;

-- 3) wearing_image_url = (id + 100) 아이템의 image_url을 base에 채움
UPDATE item base
    JOIN item wear ON wear.id = base.id + 100
    SET base.wearing_image_url = COALESCE(base.wearing_image_url, wear.image_url)
WHERE base.id BETWEEN 1 AND 46;

-- 3-1) 매칭 없는 경우(배경 등) shop으로 fallback
UPDATE item
SET wearing_image_url = shop_image_url
WHERE id BETWEEN 1 AND 46
  AND wearing_image_url IS NULL;

-- =========================================================
-- 4) FK 참조 테이블 정리
-- =========================================================

-- 4-1) base + dup 둘 다 있으면 equipped OR로 합치기
UPDATE member_item base
    JOIN member_item dup
ON dup.member_id = base.member_id
    AND dup.item_id = base.item_id + 100
    SET base.is_equipped =
        (CAST(base.is_equipped AS UNSIGNED) | CAST(dup.is_equipped AS UNSIGNED))
WHERE base.item_id BETWEEN 1 AND 46
  AND dup.item_id BETWEEN 101 AND 146;

-- 4-2) dup 행 삭제(이미 base로 합쳤으므로)  ※ base 범위도 같이 잠그기(안전)
DELETE dup
FROM member_item dup
JOIN member_item base
  ON base.member_id = dup.member_id
 AND base.item_id = dup.item_id - 100
WHERE dup.item_id BETWEEN 101 AND 146
  AND base.item_id BETWEEN 1 AND 46;

-- 4-3) member_item의 100번대 참조를 -100으로 이동
UPDATE member_item mi
    JOIN item base ON base.id = mi.item_id - 100
    SET mi.item_id = mi.item_id - 100
WHERE mi.item_id BETWEEN 101 AND 146;

-- 4-4) wallet_history의 100번대 참조를 -100으로 이동
UPDATE wallet_history wh
    JOIN item base ON base.id = wh.item_id - 100
    SET wh.item_id = wh.item_id - 100
WHERE wh.item_id BETWEEN 101 AND 146;

-- 5) 101~146(착용용) 삭제: “짝 있는 것만”
DELETE wear
FROM item wear
JOIN item base ON base.id = wear.id - 100
WHERE base.id BETWEEN 1 AND 46
  AND wear.id BETWEEN 101 AND 146;

-- 6) NOT NULL 강제 + 기존 컬럼 제거
ALTER TABLE item
    MODIFY shop_image_url VARCHAR(1000) NOT NULL,
    MODIFY wearing_image_url VARCHAR(1000) NOT NULL,
DROP COLUMN image_url;
