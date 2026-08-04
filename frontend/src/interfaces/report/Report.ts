export type ReportTargetType = "USER" | "ITEM" | "TRADE_PROPOSAL";
export type ReportStatus = "PENDING" | "REVIEWED" | "RESOLVED" | "DISMISSED";

export interface ReportRequest {
    targetType: ReportTargetType;
    targetId: number;
    reason: string;
    details?: string;
}

export interface ReportReviewRequest {
    status: ReportStatus;
    adminNotes?: string;
}

export interface ReportResponse {
    id: number;
    targetType: ReportTargetType;
    targetId: number;
    reporterId: number;
    reporterEmail: string;
    reason: string;
    details?: string | null;
    status: ReportStatus;
    adminNotes?: string | null;
    reviewedById?: number | null;
    reviewedByEmail?: string | null;
    reviewedAt?: string | null;
    createdAt?: string;
}
