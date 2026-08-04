export interface ItemResponse{
    id: number;
    name: string;
    description: string;
    categoryName: string;
    condition: ItemCondition;
    userName: string;
    createdAt?: string;
    imageUrl?: string;
    imageUrls?: string[];
    status: ItemStatus;
    user_id: number;
    category_id?: number;
    favorite?: boolean;
    rejectionReason?: string;
    moderatedById?: number;
    moderatedByEmail?: string;
    moderatedAt?: string;
}

export enum ItemCondition {
    NEW = "NEW",
    USED = "USED",
}

export enum ItemStatus {
    PENDING_REVIEW = "PENDING_REVIEW",
    APPROVED = "APPROVED",
    REJECTED = "REJECTED",
    RESERVED = "RESERVED",
    EXCHANGED = "EXCHANGED",
    ARCHIVED = "ARCHIVED",
}
