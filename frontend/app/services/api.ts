import axios from "axios";

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080/videostreamingpoc"
});

// 🔹 Log every request
api.interceptors.request.use((config) => {
    console.log("📤 Axios Request:", {
        method: config.method?.toUpperCase(),
        url: config.baseURL + config.url,
        headers: config.headers,
        data: config.data,
    });
    return config;
});

// 🔹 Log every response
api.interceptors.response.use(
    (response) => {
        console.log("✅ Axios Response:", {
            status: response.status,
            url: response.config.baseURL + response.config.url,
            headers: response.headers,
        });
        return response;
    },
    (error) => {
        if (error.response) {
            console.error("❌ Axios Error Response:", {
                status: error.response.status,
                url: error.config.baseURL + error.config.url,
                data: error.response.data,
            });
        } else {
            console.error("❌ Axios Error (no response):", error.message);
        }
        return Promise.reject(error);
    }
);


export default api;