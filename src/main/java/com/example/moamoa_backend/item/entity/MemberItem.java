package com.example.moamoa_backend.item.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.item.exception.ItemException;
import com.example.moamoa_backend.item.exception.code.ItemErrorCode;
import com.example.moamoa_backend.member.entity.Member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_member_item", columnNames = {"member_id", "item_id"})
})
public class MemberItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private boolean isEquipped;

    private MemberItem(Member member, Item item, boolean isEquipped) {
        if (member == null) {
            throw new ItemException(ItemErrorCode.MEMBER_ITEM_MEMBER_NULL);
        }
        if (item == null) {
            throw new ItemException(ItemErrorCode.MEMBER_ITEM_ITEM_NULL);
        }
        this.member = member;
        this.item = item;
        this.isEquipped = isEquipped;
    }

    /** 기본 생성: 구매 직후는 항상 미착용(false) */
    public static MemberItem create(Member member, Item item) {
        return new MemberItem(member, item, false);
    }

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }
}
