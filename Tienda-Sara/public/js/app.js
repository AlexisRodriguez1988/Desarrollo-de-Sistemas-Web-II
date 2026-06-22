window.onload = function(){

    console.log("Tienda Sara cargada correctamente");

}

function agregarProducto(producto){

    alert(producto + " agregado al carrito");

}

function aplicarCupon(){

    let cupon = document.getElementById("cupon").value;

    if(cupon === "SARA10"){

        alert("Cupón aplicado: 10% de descuento");

    }else{

        alert("Cupón no válido");

    }

}

function actualizarTotal(){

    let cantidad1 = document.getElementById("cant1").value;
    let cantidad2 = document.getElementById("cant2").value;

    let total = (15000 * cantidad1) + (1200 * cantidad2);

    document.getElementById("total").innerHTML =
        "$" + total.toLocaleString();

}