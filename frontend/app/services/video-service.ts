import api from "./api";

interface ApiCommonResponse<T> {
    code: number;
    message: string;
    data: T | null;
}

class VideoService {
    async getVideoUri(fileName: string): Promise<string | null> {
        try {
            const res = await api.get<ApiCommonResponse<string>>(
                `/api/utilities/video/${fileName}`
            );

            if (res.data.code !== 0) {
                console.warn("Failed to fetch video URI:", res.data.message);
                return null;
            }

            return res.data.data;
        } catch (error) {
            console.error("Error fetching video URI", error);
            return null;
        }
    }
}

export default new VideoService();