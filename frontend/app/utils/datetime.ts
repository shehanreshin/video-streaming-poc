export function formatDateTimeUTC(): string {
    const d = new Date();
    const pad2 = (n: number) => n.toString().padStart(2, "0");
    return `${d.getUTCFullYear()}-${pad2(d.getUTCMonth() + 1)}-${pad2(d.getUTCDate())} ` +
        `${pad2(d.getUTCHours())}:${pad2(d.getUTCMinutes())}:${pad2(d.getUTCSeconds())}`;
}