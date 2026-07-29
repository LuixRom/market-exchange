export interface ItemResponse{
    id: number;
    name: string;
    description: string;
    categoryName: string;
    condition: Condition;
    userName: string;
    imageUrl: string;
    status: Condition;
    user_id: number;
}

export enum Condition{
    PENDING = "PENDING",
    APPROVED = "APPROVED",
    REJECTED = "REJECTED",
}