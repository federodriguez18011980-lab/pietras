package Modelo;

import java.util.List;

public class WooOrder {
    public int id;
    public String status;
    public String date_created;
    public String total;
    public Billing billing;
    public List<LineItem> line_items;

    public class Billing {
        public String first_name;
        public String last_name;
        public String email;
        public String phone;
    }

    public class LineItem {
        public int product_id;
        public int quantity;
        public String name;
        public String price;
        public String sku;
    }
}
