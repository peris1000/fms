let socket;

export function connectTripWebSocket(onMessage) {
    socket = new WebSocket(import.meta.env.VITE_WS_URL);

    socket.onopen = () => {
        console.log("Connected to trip WS");
    };

    socket.onmessage = (event) => {
        const heartbeat = JSON.parse(event.data);
        onMessage(heartbeat);
    };

    socket.onerror = (e) => {
        console.error("WS error", e);
    };

    socket.onclose = () => {
        console.log("WS closed");
    };
}

export function disconnectTripWebSocket() {
    if (socket) socket.close();
}
