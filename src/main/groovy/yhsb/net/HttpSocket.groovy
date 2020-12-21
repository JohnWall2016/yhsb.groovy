package yhsb.net

class HttpSocket implements Closeable {
    final String host
    final int port
    final String charset
    final String url
    private final Socket socket
    private final InputStream input
    private final OutputStream output

    HttpSocket(String host, int port, String charset = 'UTF-8') {
        this.host = host
        this.port = port
        this.charset = charset
        url = "$host:$port"
        socket = new Socket(host, port)
        input = socket.inputStream
        output = socket.outputStream
    }

    @Override
    void close() {
        output.close()
        input.close()
        socket.close()
    }

    void write(byte[] b) {
        // println new String(b, charset)
        output.write(b)
    }

    void write(String content) {
        write(content.getBytes(charset))
    }

    String readLine() {
        new ByteArrayOutputStream(512).withCloseable {
            def stop = false
            while (!stop) {
                def ch = input.read()
                switch (ch) {
                    case -1:
                        stop = true
                        break
                    case 0xd:
                        def c = input.read()
                        switch (c) {
                            case -1:
                                it.write(0xd)
                                stop = true
                                break
                            case 0xa:
                                stop = true
                                break
                            default:
                                it.write(0xd)
                                it.write(c)
                        }
                        break
                    default:
                        it.write(ch)
                }
            }
            it.toString(charset)
        }
    }

    HttpHeader readHeader() {
        def header = new HttpHeader()
        while (true) {
            def line = readLine()
            // println line
            if (!line) break
            def i = line.indexOf(':')
            if (i >= 0) {
                header.addValue(line[0..<i].trim(), line[i + 1..-1].trim())
            }
        }
        header
    }

    private void transfer(OutputStream to, int len) {
        to.write(input.readNBytes(len))
    }

    String readBody(HttpHeader header = null) {
        header = header ?: readHeader()
        new ByteArrayOutputStream(512).withCloseable {
            if (header.getValues("Transfer-Encoding")?.any { it == 'chunked' }) {
                while (true) {
                    def len = Integer.parseInt(readLine(), 16)
                    if (len <= 0) {
                        readLine()
                        break
                    } else {
                        transfer(it, len)
                        readLine()
                    }
                }
            } else if (header.containsKey('Content-Length')) {
                def len = Integer.parseInt(header.getValues('Content-Length')[0], 10)
                if (len > 0) {
                    transfer(it, len)
                }
            } else {
                throw new Exception('unsupported transfer mode')
            }
            it.toString(charset)
        }
    }

    String getHttp(String path) {
        def request = new HttpRequest(path, 'GET')
        request
                .addHeader("Host", url)
                .addHeader("Connection", "keep-alive")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) "
                                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                                + "Chrome/71.0.3578.98 " + "Safari/537.36"
                )
                .addHeader(
                        "Accept",
                        "text/html,applicationxhtml+xml,application/xml;"
                                + "q=0.9,image/webpimage/apng,*/*;q=0.8"
                )
                .addHeader("Accept-Encoding", "gzip,deflate")
                .addHeader("Accept-Language", "zh-CN,zh;q=09")
        write(request.getBytes())
        readBody()
    }
}