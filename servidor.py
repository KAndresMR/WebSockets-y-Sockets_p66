import asyncio
import websockets
import traceback

# Conjuntos donde se almacenan las conexiones activas de WebSocket y TCP
clientes_websocket = set()
clientes_tcp = set()

# ---- Parte WebSocket ----
# Esta función maneja los clientes que se conectan mediante WebSocket
async def manejar_websocket(ws):
    clientes_websocket.add(ws)
    print("🌐 Cliente WebSocket conectado.")
    try:
        # Espera y maneja cada mensaje recibido desde el cliente WebSocket
        async for msg in ws:
            print(f"[WebSocket] 📩 {msg}")

            # Reenviar el mensaje a otros clientes WebSocket
            for c_ws in set(clientes_websocket):
                if c_ws != ws:
                    try:
                        await c_ws.send(f"[WS] {msg}")
                    except Exception as e:
                        print(f"❌ Error reenviando a WS: {e}")
                        clientes_websocket.discard(c_ws)

            # Reenviar el mensaje a todos los clientes TCP conectados
            for c_tcp in set(clientes_tcp):
                try:
                    c_tcp.write(f"[WS] {msg}\n".encode())
                except Exception as e:
                    print(f"⚠️ Error en write TCP: {e}")
                    clientes_tcp.discard(c_tcp)
                    continue
                try:
                    await c_tcp.drain()
                except Exception as e:
                    print(f"⚠️ Error en drain TCP: {e}")
                    clientes_tcp.discard(c_tcp)
    except Exception as e:
        print(f"❌ Error en cliente WS: {e}")
        traceback.print_exc()
    finally:
        clientes_websocket.discard(ws)
        await ws.close()
        print("🌐 Cliente WebSocket desconectado.")

# ---- Parte TCP ----
# Esta función maneja los clientes que se conectan mediante TCP
async def manejar_tcp(reader, writer):
    clientes_tcp.add(writer)
    addr = writer.get_extra_info('peername')
    print(f"🔌 Cliente TCP conectado desde {addr}")
    try:
        while True:
            data = await reader.readline()
            if not data:
                break
            msg = data.decode().strip()
            print(f"[TCP] 📩 {msg}")

            # Reenviar el mensaje a otros clientes TCP
            for c_tcp in set(clientes_tcp):
                if c_tcp != writer:
                    try:
                        c_tcp.write(f"[TCP] {msg}\n".encode())
                        await c_tcp.drain()
                    except Exception as e:
                        print(f"❌ Error reenviando a TCP: {e}")
                        clientes_tcp.discard(c_tcp)

            # Reenviar el mensaje a todos los clientes WebSocket
            for c_ws in set(clientes_websocket):
                try:
                    await c_ws.send(f"[TCP] {msg}")
                except Exception as e:
                    print(f"❌ Error reenviando a WS: {e}")
                    clientes_websocket.discard(c_ws)
    except Exception as e:
        print(f"❌ Error en cliente TCP {addr}: {e}")
    finally:
        clientes_tcp.discard(writer)
        writer.close()
        await writer.wait_closed()
        print(f"🔌 Cliente TCP desconectado desde {addr}")

# ---- Main ----
# Esta función principal lanza el servidor WebSocket y TCP en paralelo
async def main():
    ws_server = await websockets.serve(manejar_websocket, "0.0.0.0", 8765)
    tcp_server = await asyncio.start_server(manejar_tcp, "0.0.0.0", 12345)
    print("Servidor escuchando WS en 8765 y TCP en 12345...")
    await asyncio.gather(ws_server.wait_closed(), tcp_server.serve_forever())

# Iniciar el servidor
asyncio.run(main())
