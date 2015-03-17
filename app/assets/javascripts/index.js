$(function() {

  function updateCart(data) {
    var cartItems = $("#cartItems");
    cartItems.empty();
    $.each(data.items, function(index, item) {
      var row = $("<tr>");
      row.append($("<td>").text(item.name));
      row.append($("<td>").text(item.price));
      row.append($("<td>").text(item.quantity));
      cartItems.append(row);
    });
  }

  $.getJSON("/api/userprofile", function(data) {
    $("#profileHeading").toggleClass("active");
    $("#profileName").text(data.name);
    $("#profileEmail").text(data.email);
    $("#profileRole").text(data.role);
  });

  $.getJSON("/api/cart", function(data) {
    $("#cartHeading").toggleClass("active");
    updateCart(data);
  });

  $("#addToCartForm").submit(function(event) {
    event.preventDefault();

    $("#addToCartButton").toggleClass("active");

    $.ajax("/api/cart", {
      data: {
        name: $("#cartItemName").val(),
        price: $("#cartItemPrice").val(),
        quantity: $("#cartItemQuantity").val()
      },
      method: "post",
      success: function(data) {
        $("#addToCartButton").toggleClass("active");
        $("#cartItemName").val("");
        $("#cartItemPrice").val("");
        $("#cartItemQuantity").val("");
        updateCart(data);
      }
    });
  });

});