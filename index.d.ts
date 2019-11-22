export const enum Cast {
    broadcast = "broadcast",
    multicast = "multicast",
}

type Options = {
    ssid: string
    password: number
    count: number
    bssid: string
    cast: Cast
}

export function start(options?: Options): Promise<Array | null>;
export function stop(): void;