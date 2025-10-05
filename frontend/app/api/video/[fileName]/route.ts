import { NextRequest, NextResponse } from "next/server";
import VideoService from "@/app/services/video-service";

export async function GET(
    req: NextRequest,
    context: { params: Promise<{ fileName: string }> }
) {
    const { fileName } = await context.params;
    const range = req.headers.get("range") ?? undefined;

    try {
        const response = await VideoService.getVideoStream(fileName, range);

        const headers = new Headers();
        Object.entries(response.headers).forEach(([key, value]) => {
            if (value) headers.set(key, String(value));
        });

        return new NextResponse(response.data, {
            status: response.status,
            headers,
        });
    } catch (error: any) {
        console.error("Video proxy error:", error.message);
        return NextResponse.json({ error: "Failed to proxy video" }, { status: 500 });
    }
}