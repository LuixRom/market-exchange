export interface RatingRequest {
    tradeProposalId: number;
    score: number;
    communicationScore?: number;
    punctualityScore?: number;
    itemConditionScore?: number;
    comment?: string;
}

export interface RatingResponse {
    id: number;
    tradeProposalId: number;
    score: number;
    communicationScore?: number | null;
    punctualityScore?: number | null;
    itemConditionScore?: number | null;
    comment?: string | null;
    reviewerId: number;
    reviewerName: string;
    reviewedUserId: number;
    reviewedUserName: string;
    createdAt?: string;
    updatedAt?: string | null;
}

export interface RatingReputation {
    userId: number;
    averageScore?: number | null;
    ratingCount: number;
}
