package me.miguelsolano.unabstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickLogout: () -> Unit = {},
    productViewModel: ProductViewModel = viewModel()
) {
    val auth = Firebase.auth
    val user = auth.currentUser

    var showAddDialog by remember { mutableStateOf(false) }
    var productos by remember { mutableStateOf(emptyList<Producto>()) }
    var mensaje by remember { mutableStateOf("") }

    val loadProducts: () -> Unit = {
        productViewModel.obtenerProductos { newProducts ->
            productos = newProducts
        }
    }

    LaunchedEffect(Unit) { loadProducts() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFFEBCD), Color(0xFFFDFDFD)))
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 CABECERA
        Text(
            text = "HOME SCREEN",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B1B1B)
        )
        Text(
            text = "Usuario: ${user?.email ?: "Invitado"}",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.signOut()
                onClickLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(45.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔹 SECCIÓN DE PRODUCTOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis Productos",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1B1B1B)
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004AAD)),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Agregar", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (productos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay productos",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = "Haz click en Agregar para crear uno",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }
        } else {
            ProductList(
                productos = productos,
                onDelete = { id ->
                    productViewModel.eliminarProducto(id) { success ->
                        mensaje = if (success) "Producto eliminado correctamente." else "Error al eliminar el producto."
                        loadProducts()
                    }
                }
            )
        }

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (mensaje.contains("Error")) Color.Red else Color(0xFF00796B),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            LaunchedEffect(mensaje) {
                delay(3000L)
                mensaje = ""
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { producto ->
                productViewModel.agregarProducto(producto) { success, msg ->
                    showAddDialog = false
                    mensaje = msg
                    if (success) loadProducts()
                }
            }
        )
    }
}

@Composable
fun ProductList(productos: List<Producto>, onDelete: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 450.dp)
    ) {
        items(productos, key = { it.id ?: UUID.randomUUID().toString() }) { producto ->
            ProductItem(producto = producto, onDelete = onDelete)
        }
    }
}

@Composable
fun ProductItem(producto: Producto, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1B1B1B)
                )
                Text(
                    text = producto.descripcion,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$ ${String.format("%.0f", producto.precio)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFFFF9900)
                )
            }
            IconButton(onClick = { producto.id?.let { onDelete(it) } }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onAdd: (Producto) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = precioText,
                    onValueChange = { precioText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precio = precioText.toDoubleOrNull()
                    if (nombre.isBlank() || precio == null || precio <= 0) {
                        errorMsg = "Completa todos los campos correctamente."
                    } else {
                        onAdd(Producto(nombre = nombre.trim(), descripcion = descripcion.trim(), precio = precio))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900))
            ) { Text("Guardar", color = Color.White) }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
