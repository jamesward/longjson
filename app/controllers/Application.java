package controllers;

import models.CartItem;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;
import services.CartService;
import services.UserProfileService;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render());
    }

    public static Result userprofile() {
        Chunks<String> chunks = new ChunkBlower(10, UserProfileService.get().map( userprofile ->
                        Json.toJson(userprofile).toString()
            )
        );

        return ok(chunks).as(Http.MimeTypes.JSON);
    }

    public static Result cart() {
        Chunks<String> chunks = new ChunkBlower(10, CartService.get().map( cart ->
                        Json.toJson(cart).toString()
            )
        );

        return ok(chunks).as(Http.MimeTypes.JSON);
    }

    @BodyParser.Of(BodyParser.FormUrlEncoded.class)
    public static Result addToCart() {
        DynamicForm requestData = Form.form().bindFromRequest();

        CartItem cartItem = new CartItem(requestData.get("name"), new Double(requestData.get("price")), new Integer(requestData.get("quantity")));

        Chunks<String> chunks = new ChunkBlower(10, CartService.addToCart(cartItem).flatMap( result ->
                        CartService.get().map( cart -> Json.toJson(cart).toString())
            )
        );

        return ok(chunks).as(Http.MimeTypes.JSON);
    }
}
