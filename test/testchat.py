import asyncio

usuarios = {}  # username: writer
salas = {}    # sala: set(username)

async def manejar_cliente(reader, writer):
    addr = writer.get_extra_info("peername")
    print(f"📲 Nueva conexión desde {addr}")

    writer.write(b"Ingrese su nombre de usuario:\n")
    await writer.drain()
    nombre = (await reader.readline()).decode().strip()

    if not nombre:
        writer.close()
        await writer.wait_closed()
        return

    usuarios[nombre] = writer

    writer.write(b"Ingrese el nombre de la sala a la que desea unirse:\n")
    await writer.drain()
    sala = (await reader.readline()).decode().strip()

    if not sala:
        sala = "general"

    if sala not in salas:
        salas[sala] = set()
    salas[sala].add(nombre)

    writer.write(f"✅ Bienvenido {nombre}, te uniste a la sala '{sala}'\n".encode())
    await writer.drain()

    try:
        while True:
            data = await reader.readline()
            if not data:
                break
            msg = data.decode().strip()

            mensaje_formateado = f"[{nombre} - {sala}]: {msg}\n"
            print(mensaje_formateado.strip())
            
            # 🔽 Guardar el mensaje en un archivo por sala
            nombre_archivo = f"historial_{sala}.txt"
            try:
                with open(nombre_archivo, "a", encoding="utf-8") as f:
                    f.write(mensaje_formateado)
            except Exception as e:
                print(f"❌ Error guardando historial: {e}")
            
            for usuario_en_sala in salas[sala]:
                if usuario_en_sala != nombre and usuario_en_sala in usuarios:
                    try:
                        usuarios[usuario_en_sala].write(mensaje_formateado.encode())
                        await usuarios[usuario_en_sala].drain()
                    except:
                        continue
    except Exception as e:
        print(f"❌ Error con {nombre}: {e}")
    finally:
        print(f"🔌 {nombre} desconectado")
        writer.close()
        await writer.wait_closed()
        if nombre in usuarios:
            del usuarios[nombre]
        if sala in salas and nombre in salas[sala]:
            salas[sala].remove(nombre)

async def main():
    server = await asyncio.start_server(manejar_cliente, "0.0.0.0", 12345)
    addr = server.sockets[0].getsockname()
    print(f"💬 Servidor escuchando en {addr}")
    async with server:
        await server.serve_forever()

asyncio.run(main())
