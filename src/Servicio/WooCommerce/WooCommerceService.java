package Servicio.WooCommerce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Servicio base para la API REST de WooCommerce v3.
 * No requiere librerías externas — usa solo java.net y java.io.
 *
 * USO:
 *   WooCommerceService wc = new WooCommerceService();
 *   String json          = wc.get("products?per_page=20");
 *   String creado        = wc.post("products", jsonBody);
 *   String actualizado   = wc.put("products/123", jsonBody);
 */
public class WooCommerceService {

    // =========================================================
    //  CONFIGURACION — completar con los datos reales del sitio
    // =========================================================
    private static final String BASE_URL        = "https://joyeriapietras.com.ar/wp-json/wc/v3/";
    private static final String CONSUMER_KEY    = "ck_e33b4bd6f9a15a933aacd47f0e14a62913d0340d";
    private static final String CONSUMER_SECRET = "cs_28ab4e845ca2bc4f8188137e2a9c2e81754ab21b";
    private static final int    TIMEOUT_MS      = 10000; // 10 segundos
    // =========================================================

    private final String credenciales;

    public WooCommerceService() {
        String raw = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        this.credenciales = Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // --- METODOS PUBLICOS ---

    public String get(String endpoint) throws IOException {
        return leerRespuesta(abrirConexion(endpoint, "GET"));
    }

    public String post(String endpoint, String jsonBody) throws IOException {
        HttpURLConnection con = abrirConexion(endpoint, "POST");
        enviarCuerpo(con, jsonBody);
        return leerRespuesta(con);
    }

    public String put(String endpoint, String jsonBody) throws IOException {
        HttpURLConnection con = abrirConexion(endpoint, "PUT");
        enviarCuerpo(con, jsonBody);
        return leerRespuesta(con);
    }

    public String delete(String endpoint) throws IOException {
        return leerRespuesta(abrirConexion(endpoint, "DELETE"));
    }

    // --- METODOS INTERNOS ---

    private HttpURLConnection abrirConexion(String endpoint, String metodo) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(metodo);
        con.setRequestProperty("Authorization", "Basic " + credenciales);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Accept", "application/json");
        con.setConnectTimeout(TIMEOUT_MS);
        con.setReadTimeout(TIMEOUT_MS);
        if ("POST".equals(metodo) || "PUT".equals(metodo)) {
            con.setDoOutput(true);
        }
        return con;
    }

    private void enviarCuerpo(HttpURLConnection con, String jsonBody) throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String leerRespuesta(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                status >= 200 && status < 300
                        ? con.getInputStream()
                        : con.getErrorStream(),
                StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String linea;
        while ((linea = reader.readLine()) != null) sb.append(linea);
        reader.close();
        con.disconnect();
        if (status < 200 || status >= 300) {
            throw new IOException("WooCommerce API error [HTTP " + status + "]: " + sb);
        }
        return sb.toString();
    }
}