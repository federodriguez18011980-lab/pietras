package Servicios;

import Vista.Sistema;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JOptionPane;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servicio encargado de verificar si existen nuevas versiones del sistema
 * publicadas en GitHub Releases de forma automática y asíncrona.
 */
public class ActualizadorService {

    // Versión actual local del sistema
    private final String VERSION_ACTUAL = "1.1";
    
    // Propietario y repositorio en GitHub. Ajustar a los valores reales del usuario.
    private final String GITHUB_OWNER = "Federico18011980";
    private final String GITHUB_REPO = "CanariasSistem"; 

    private Vista.Sistema vistaPrincipal;

    public ActualizadorService(Vista.Sistema vistaPrincipal) {
        this.vistaPrincipal = vistaPrincipal;
    }
    
    /**
     * Verifica la última versión publicada en GitHub Releases de manera asíncrona.
     * Si detecta una nueva versión, le ofrece la descarga al usuario.
     */
    public void verificarVersion() {
        new Thread(() -> {
            try {
                // URL de la API de GitHub para la última release del repositorio
                String urlApi = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
                URL url = new URL(urlApi);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                // IMPORTANTE: Definir un User-Agent. GitHub bloquea peticiones sin esta cabecera (Error 403).
                conn.setRequestProperty("User-Agent", "PietrasUpdater/1.1");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    JsonObject releaseJson = JsonParser.parseReader(reader).getAsJsonObject();
                    reader.close();

                    if (releaseJson.has("tag_name") && !releaseJson.get("tag_name").isJsonNull()) {
                        String versionRemota = releaseJson.get("tag_name").getAsString();
                        
                        // URL base de fallback en caso de no haber assets en la release
                        String urlDescarga = releaseJson.has("html_url") 
                                ? releaseJson.get("html_url").getAsString() 
                                : "https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases";

                        // Si hay assets subidos, obtenemos la URL de descarga directa del primer archivo (.exe o .jar)
                        if (releaseJson.has("assets") && !releaseJson.get("assets").isJsonNull()) {
                            JsonArray assets = releaseJson.getAsJsonArray("assets");
                            if (assets.size() > 0) {
                                JsonObject firstAsset = assets.get(0).getAsJsonObject();
                                if (firstAsset.has("browser_download_url")) {
                                    urlDescarga = firstAsset.get("browser_download_url").getAsString();
                                }
                            }
                        }

                        // Comparación semántica e inteligente de las versiones
                        if (esVersionMasNueva(VERSION_ACTUAL, versionRemota)) {
                            final String finalUrlDescarga = urlDescarga;
                            final String finalVersionRemota = versionRemota;
                            
                            // Mostrar cuadro de diálogo interactivo en el Event Dispatch Thread (hilo de Swing UI)
                            java.awt.EventQueue.invokeLater(() -> {
                                int respuesta = JOptionPane.showConfirmDialog(vistaPrincipal, 
                                    "¡Nueva versión disponible (" + finalVersionRemota + ")!\n" +
                                    "¿Desea descargar la actualización ahora?\n" +
                                    "(El sistema ejecutará un respaldo y se cerrará de forma segura para la instalación).", 
                                    "Actualización del Sistema", 
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE);

                                if (respuesta == JOptionPane.YES_OPTION) {
                                    try {
                                        // Abrir la descarga en el navegador por defecto
                                        Desktop.getDesktop().browse(new java.net.URI(finalUrlDescarga));
                                        
                                        // Ejecutar salida controlada (backup automático de seguridad y cierre seguro de base de datos)
                                        vistaPrincipal.salirDelSistema();
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(vistaPrincipal, 
                                            "No se pudo abrir el enlace de descarga: " + ex.getMessage(), 
                                            "Error de Actualización", 
                                            JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                        }
                    }
                } else {
                    System.out.println("Actualizaciones: No se pudo verificar la versión en GitHub. Código HTTP: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                System.out.println("Error al verificar actualizaciones en segundo plano: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Limpia la versión de prefijos no deseados como 'v' o 'V'.
     */
    private String limpiarVersion(String version) {
        if (version == null) return "";
        version = version.trim().toLowerCase();
        if (version.startsWith("v")) {
            version = version.substring(1);
        }
        return version;
    }

    /**
     * Compara semánticamente la versión local contra la versión remota.
     * Retorna true si la versión remota es superior a la local.
     */
    private boolean esVersionMasNueva(String local, String remota) {
        String cleanLocal = limpiarVersion(local);
        String cleanRemota = limpiarVersion(remota);
        
        String[] partesLocal = cleanLocal.split("\\.");
        String[] partesRemota = cleanRemota.split("\\.");
        
        int longitud = Math.max(partesLocal.length, partesRemota.length);
        for (int i = 0; i < longitud; i++) {
            int vLocal = i < partesLocal.length ? parseSafeInt(partesLocal[i]) : 0;
            int vRemota = i < partesRemota.length ? parseSafeInt(partesRemota[i]) : 0;
            
            if (vRemota > vLocal) {
                return true; // La remota es mayor
            }
            if (vLocal > vRemota) {
                return false; // La local es mayor o igual
            }
        }
        return false; // Son idénticas
    }

    /**
     * Parseo seguro a número entero.
     */
    private int parseSafeInt(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}