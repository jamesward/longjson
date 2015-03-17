package services;

import models.Cart;
import models.CartItem;
import play.libs.F;
import services.ws.CartWS;
import utils.MemCachierClient;

public class CartService {

    public static F.Promise<Cart> get() {
        Cart maybeCart = (Cart) MemCachierClient.getInstance().get("cart");
        if (maybeCart == null) {
            return F.Promise.wrap(CartWS.get()).map( cart -> {
                MemCachierClient.getInstance().set("cart", 60 * 60, cart);
                return cart;
            });
        }
        else {
            return F.Promise.pure(maybeCart);
        }
    }

    public static F.Promise addToCart(CartItem cartItem) {
        return F.Promise.wrap(CartWS.addItem(cartItem)).map( nothing -> {
            MemCachierClient.getInstance().delete("cart").get(); // block
            return nothing;
        });
    }

}
