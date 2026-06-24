package Servicio.WooCommerce;

import Modelo.Productos;
import Modelo.ProductosDao;
import Modelo.WooOrder;
import java.io.IOException;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SincronizadorWooCommerce {
    
    private WooCommerceService wcService = new WooCommerceService();

    /**
     * Actualiza el stock en la web usando el SKU (cod_pro) o ID de WC
     */
    public boolean actualizarStockWeb(int idWooCommerce, int nuevoStock) {
        try {
            // El formato que pide WC para actualizar stock es: {"manage_stock":true, "stock_quantity": 50}
            String jsonBody = "{\"stock_quantity\": " + nuevoStock + "}";
            
            // Endpoint: products/<id>
            String respuesta = wcService.put("products/" + idWooCommerce, jsonBody);
            
            System.out.println("✅ Sincronizado en PIETRAS Web: " + respuesta);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al sincronizar: " + e.getMessage());
            return false;
        }
    }
    
    public void vincularProductosLocalesConWeb() {
        ProductosDao proDao = new ProductosDao();
        List<Productos> listaLocal = proDao.ListarProductos(); // Tu método actual

        for (Productos p : listaLocal) {
            String sku = p.getCodigo(); // Usamos el código como SKU

            try {
                // Buscamos en la API por SKU específico
                // Endpoint: products?sku=XXXX
                String jsonRespuesta = wcService.get("products?sku=" + sku);

                if (jsonRespuesta.contains("\"id\":")) {
                    // Extraemos el ID (Lógica simple: buscamos lo que está después de "id":)
                    int inicio = jsonRespuesta.indexOf("\"id\":") + 5;
                    int fin = jsonRespuesta.indexOf(",", inicio);
                    String idWcStr = jsonRespuesta.substring(inicio, fin).trim();

                    int idWooCommerce = Integer.parseInt(idWcStr);

                    // Actualizamos en tu DB local
                    if (proDao.actualizarIdWooCommerce(p.getId(), idWooCommerce)) {
                        System.out.println("✅ Vinculado: " + p.getDescripcion()+ " -> WC_ID: " + idWooCommerce);
                    }
                } else {
                    System.out.println("⚠️ El producto " + sku + " no existe en la web.");
                }

            } catch (IOException e) {
                System.err.println("❌ Error al vincular " + sku + ": " + e.getMessage());
            }
        }
    }
    
    //--METODO PARA SINCRONIZAR EL STOCK DESDE LA WEB AL SISTEMA--//
    
    public void igualarStockDesdeWeb() {
        ProductosDao proDao = new ProductosDao();
        List<Productos> lista = proDao.ListarProductos();

        for (Productos p : lista) {
            if (p.getIdWooCommerce() > 0) { // Solo si está vinculado
                try {
                    String json = wcService.get("products/" + p.getIdWooCommerce());

                    // Buscamos "stock_quantity":XX en el JSON
                    int inicio = json.indexOf("\"stock_quantity\":") + 17;
                    int fin = json.indexOf(",", inicio);
                    String stockStr = json.substring(inicio, fin).trim();

                    if (!stockStr.equals("null")) {
                        int stockWeb = Integer.parseInt(stockStr);
                        // Actualizamos tu base de datos local
                        proDao.actualizarStockFisico(p.getId(), stockWeb);
                        System.out.println("🔄 " + p.getDescripcion() + " actualizado a " + stockWeb + " unidades.");
                    }
                } catch (Exception e) {
                    System.err.println("Error al traer stock: " + e.getMessage());
                }
            }
        }
    }
    
    //--SUBIR EL STOCK DESDE EL SISTEMA HACIA LA WEB--//
    
    public void subirStockAWeb(int idWC, int stockLocal) {
        new Thread(() -> { // Lo hacemos en un hilo aparte para que el sistema no se tilde si internet está lento
            try {
                String jsonBody = "{\"stock_quantity\": " + stockLocal + "}";
                wcService.put("products/" + idWC, jsonBody);
                System.out.println("✅ Web actualizada con stock: " + stockLocal);
            } catch (IOException e) {
                System.err.println("❌ Falló actualización web: " + e.getMessage());
            }
        }).start();
    }
    
    //--SINCRONIZAMOS LOS PRODUCTOS DE AMBOS ECOSISTEMAS, SISTEMA Y WEB--//
    
  //--SINCRONIZAMOS LOS PRODUCTOS DE AMBOS ECOSISTEMAS, SISTEMA Y WEB (STOCK Y PRECIO)--//
    
    public void sincronizarProducto(Productos p) {
        if (p.getIdWooCommerce() <= 0) {
            System.out.println("El getIDWooCommerce devolvió 0" + p.getIdWooCommerce());
            return;
        }

        new Thread(() -> {
            try {
                // 1. Forzar formato de precio con punto decimal usando la configuración regional de US
                // Esto transforma un "1250,50" local en un "1250.50" que WooCommerce entiende
                String precioFormateado = String.format(java.util.Locale.US, "%.2f", p.getPrecioxdolar());
                
                int stockEntero = p.getStock().intValue();

                // 2. Usamos una estructura de datos limpia (JsonObject o una clase anónima)
                // Creamos un objeto Map para que Gson lo convierta a JSON automáticamente
                java.util.Map<String, Object> datosActualizar = new java.util.HashMap<>();
                datosActualizar.put("manage_stock", true);
                datosActualizar.put("stock_quantity", stockEntero);
                datosActualizar.put("regular_price", precioFormateado);

                // 3. Gson hace la magia de empaquetar todo sin errores de comillas
                Gson gson = new Gson();
                String jsonBody = gson.toJson(datosActualizar);

                // IMPORTANTE: Asegúrate de que la URL termine bien
                String respuesta = wcService.put("products/" + p.getIdWooCommerce(), jsonBody);

                System.out.println("✅ Sincronizado: " + p.getDescripcion() 
                        + " | Stock enviado: " + stockEntero 
                        + " | Precio enviado: $" + precioFormateado);

            } catch (IOException e) {
                System.err.println("❌ Error en la API de WooCommerce: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Error inesperado al procesar datos: " + e.getMessage());
            }
        }).start();
    }
    
    public void eliminarDeWeb(int idWC) {
        if (idWC > 0) {
            new Thread(() -> {
                try {
                    // En WC, el delete lo mueve a la papelera
                    wcService.delete("products/" + idWC);
                    System.out.println("🗑️ Producto eliminado de la web.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    

public void descargarPedidosWeb() {
    Gson gson = new Gson();
    ProductosDao proDao = new ProductosDao();

    try {
        // Traemos solo pedidos "procesando" (pagados)
        String jsonRespuesta = wcService.get("orders?status=processing");
        
        Type listType = new TypeToken<ArrayList<WooOrder>>(){}.getType();
        List<WooOrder> pedidos = gson.fromJson(jsonRespuesta, listType);

        for (WooOrder pedido : pedidos) {
            System.out.println("📦 Procesando Pedido Web #" + pedido.id);

            for (WooOrder.LineItem item : pedido.line_items) {
                // Buscamos el producto local usando el ID de WooCommerce
                Productos p = proDao.buscarPorIdWooCommerce(item.product_id);

                if (p != null) {
                    // 1. Restamos stock localmente
                    double nuevoStock = p.getStock().doubleValue() - item.quantity;
                    int nuevostock = (int) nuevoStock;
                    proDao.actualizarStockFisico(p.getId(), nuevostock);
                    
                    System.out.println("✅ Stock actualizado: " + p.getDescripcion()+ " (-" + item.quantity + ")");
                }
            }

            // 2. IMPORTANTE: Cambiamos el estado a 'completed' en la web
            // Así evitamos descontar stock de nuevo en la próxima vuelta del monitor.
            String updateJson = "{\"status\": \"completed\"}";
            wcService.put("orders/" + pedido.id, updateJson);
            System.out.println("🏁 Pedido #" + pedido.id + " marcado como COMPLETADO.");
        }
    } catch (Exception e) {
        System.err.println("❌ Error en sincronización inversa: " + e.getMessage());
    }
}

   //-- ACTUALIZACIÓN MASIVA DE PRECIOS POR LOTES (BATCH) POST-DOLAR --//
    public void sincronizarPreciosPostDolar() {
        ProductosDao proDao = new ProductosDao();
        List<Productos> listaWeb = proDao.ListarProductosWebVinculados(); 

        if (listaWeb.isEmpty()) {
            System.out.println("ℹ️ No hay productos vinculados a la web para actualizar.");
            return;
        }

        new Thread(() -> {
            List<java.util.Map<String, Object>> loteUpdate = new ArrayList<>();
            Gson gson = new Gson();

            System.out.println("⏳ Iniciando actualización masiva en WooCommerce...");

            for (int i = 0; i < listaWeb.size(); i++) {
                Productos p = listaWeb.get(i);
                
                java.util.Map<String, Object> prodData = new java.util.HashMap<>();
                prodData.put("id", p.getIdWooCommerce());
                prodData.put("stock_quantity", p.getStock().intValue());
                
                // --- BLINDAJE DEL PRECIO ---
                // Si tu precio en 'Productos' es BigDecimal, usá: p.getPrecioxdolar().doubleValue()
                // Si ya es double, dejá p.getPrecioxdolar()
                double precioFinalLocal = p.getPrecioxdolar().doubleValue();
                
                
                try {
                    // Cambiá esta línea según si tu método devuelve Double o BigDecimal
                    
                } catch (Exception e) {
                    System.err.println("❌ Error al obtener precio del producto: " + p.getDescripcion());
                }
                

                // Forzamos estrictamente el punto decimal y 2 posiciones (Ej: 1550.50)
                String precioFormateado = String.format(java.util.Locale.US, "%.2f", precioFinalLocal);
                
                // Control en consola: Verificamos qué estamos armando antes de enviar
                System.out.println("DEBUG 🛠️ Enviando ID: " + p.getIdWooCommerce() + " | Precio Formateado: " + precioFormateado);
                
                prodData.put("regular_price", precioFormateado);
                loteUpdate.add(prodData);

                if (loteUpdate.size() == 100 || i == listaWeb.size() - 1) {
                    java.util.Map<String, Object> batchBody = new java.util.HashMap<>();
                    batchBody.put("update", loteUpdate);
                    
                    String jsonPayload = gson.toJson(batchBody);
                    
                    try {
                        String respuesta = wcService.post("products/batch", jsonPayload); 
                        System.out.println("🚀 Lote enviado con éxito (" + loteUpdate.size() + " productos).");
                        // Descomentá la línea de abajo si querés ver qué te responde la API exactamente:
                        // System.out.println("RESPUESTA API: " + respuesta);
                    } catch (IOException e) {
                        System.err.println("❌ Falló el envío del lote en WooCommerce: " + e.getMessage());
                    }
                    
                    loteUpdate.clear();
                }
            }
            System.out.println("🏁 Sincronización masiva finalizada.");
        }).start();
    }
    
    /**
     * Vincula un único producto local con la web usando su SKU/Código.
     * Si lo encuentra, actualiza el ID en la base de datos local y sube stock/precio.
     */
    public void vincularProductoIndividualConWeb(Productos p) {
        // Si ya está vinculado, no gastamos peticiones a la API
        if (p.getIdWooCommerce() > 0) {
            System.out.println("ℹ️ " + p.getDescripcion() + " ya se encuentra vinculado (ID WC: " + p.getIdWooCommerce() + ")");
            return;
        }

        new Thread(() -> {
            ProductosDao proDao = new ProductosDao();
            String sku = p.getCodigo();

            try {
                // Buscamos en la API de WooCommerce por su SKU específico
                String jsonRespuesta = wcService.get("products?sku=" + sku);

                // Convertimos la respuesta en un JsonArray usando Gson
                Gson gson = new Gson();
                com.google.gson.JsonArray arregloProductos = gson.fromJson(jsonRespuesta, com.google.gson.JsonArray.class);

                // Si el arreglo no está vacío, significa que el producto existe en la web
                if (arregloProductos != null && arregloProductos.size() > 0) {
                    // Tomamos el primer producto que coincida
                    com.google.gson.JsonObject productoWeb = arregloProductos.get(0).getAsJsonObject();
                    int idWooCommerce = productoWeb.get("id").getAsInt();

                    // Actualizamos en tu base de datos local
                    if (proDao.actualizarIdWooCommerce(p.getId(), idWooCommerce)) {
                        System.out.println("✅ Vinculado individualmente: " + p.getDescripcion() + " -> WC_ID: " + idWooCommerce);
                        
                        // Seteamos el ID en el objeto actual para que el método de abajo funcione
                        p.setIdWooCommerce(idWooCommerce);
                        
                        // Aprovechamos y le subimos el precio y stock actual de forma inmediata
                        sincronizarProducto(p);
                    }
                } else {
                    System.out.println("⚠️ El producto " + p.getDescripcion() + " (SKU: " + sku + ") no existe en la web. Creándolo como Borrador...");
                    
                    // 1. Obtener precio en pesos (en la DB local, 'precioxdolar' es el precio en pesos argentinos)
                    double precioFinalLocal = p.getPrecioxdolar() != null ? p.getPrecioxdolar().doubleValue() : 0.0;
                    String precioFormateado = String.format(java.util.Locale.US, "%.2f", precioFinalLocal);
                    
                    // 2. Construir JSON para crear el producto
                    java.util.Map<String, Object> productoJson = new java.util.HashMap<>();
                    productoJson.put("name", p.getDescripcion());
                    productoJson.put("type", "simple");
                    productoJson.put("status", "draft"); // Borrador
                    productoJson.put("sku", sku);
                    productoJson.put("regular_price", precioFormateado);
                    productoJson.put("manage_stock", true);
                    productoJson.put("stock_quantity", p.getStock().intValue());
                    
                    String jsonBody = gson.toJson(productoJson);
                    
                    // 3. POST a WooCommerce
                    String respuestaPost = wcService.post("products", jsonBody);
                    
                    // 4. Parsear respuesta y guardar ID de WooCommerce en base local
                    com.google.gson.JsonObject productoCreado = gson.fromJson(respuestaPost, com.google.gson.JsonObject.class);
                    int idWooCommerce = productoCreado.get("id").getAsInt();
                    
                    if (proDao.actualizarIdWooCommerce(p.getId(), idWooCommerce)) {
                        p.setIdWooCommerce(idWooCommerce);
                        System.out.println("✅ Creado y vinculado individualmente en WooCommerce (Borrador): " + p.getDescripcion() + " -> WC_ID: " + idWooCommerce);
                    }
                }

            } catch (IOException e) {
                System.err.println("❌ Error de red al intentar vincular " + sku + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Error inesperado en vinculación individual de " + sku + ": " + e.getMessage());
            }
        }).start();
    }

    public List<WooOrder> obtenerPedidosProcesando() throws IOException {
        String jsonRespuesta = wcService.get("orders?status=processing");
        Gson gson = new Gson();
        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<ArrayList<WooOrder>>(){}.getType();
        return gson.fromJson(jsonRespuesta, listType);
    }

    public boolean completarPedidoEnWeb(int idPedido) {
        try {
            String updateJson = "{\"status\": \"completed\"}";
            wcService.put("orders/" + idPedido, updateJson);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error al completar pedido " + idPedido + ": " + e.getMessage());
            return false;
        }
    }
    
}