package yhsb.net

class HttpRequest {
    final String path
    final String method
    final String charset

    HttpRequest(String path, String method = 'GET', String charset = 'UTF-8') {
        this.path = path
        this.method = method
        this.charset = charset
    }

    private final HttpHeader header = new HttpHeader()
    private final ByteArrayOutputStream body = new ByteArrayOutputStream(512)

    void addHeader(HttpHeader header) {
        this.header.add(header)
    }

    HttpRequest addHeader(String key, String value) {
        header[key] = value
        this
    }

    void addBody(String content) {
        body.writeBytes(content.getBytes(charset))
    }

    byte[] getBytes() {
        new ByteArrayOutputStream(512).withCloseable { buf ->
            def write = { String s -> buf.write(s.getBytes(charset)) }
            write("$method $path HTTP/1.1\r\n")
            for (e in header) {
                write("${e.key}:${e.value}\r\n")
            }
            if (body.size() > 0) {
                write("content-length: ${body.size()}\r\n")
            }
            write("\r\n")
            if (body.size() > 0) {
                body.writeTo(buf)
            }
            buf.toByteArray()
        }
    }
}

