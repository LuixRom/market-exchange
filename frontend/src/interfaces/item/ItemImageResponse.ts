export interface ItemImageResponse {
    id: number;
    itemId: number;
    imageUrl: string;
    primary: boolean;
    sortOrder: number;
    createdAt?: string;
}
