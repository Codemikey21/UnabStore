package me.miguelsolano.unabstore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val viewModel: ProductViewModel = viewModel()

    var productos by remember { mutableStateOf(listOf<Producto>()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    // ▶️ SUSCRIPCIÓN EN TIEMPO REAL
    LaunchedEffect(Unit) {
        viewModel.observarProductosTiempoReal { lista ->
            productos = lista
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header con usuario + logout
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "UNAB SHOP",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Usuario: ${user?.email ?: "Desconocido"}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        auth.signOut()
                        onClickLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de productos
        ProductosSection(
            productos = productos,
            isLoading = isLoading,
            onAgregar = { showDialog = true },
            onEliminar = { id ->
                viewModel.eliminarProducto(id) { /* gracias al listener, la lista se actualiza sola */ }
            }
        )

        if (showDialog) {
            AddProductoDialog(
                onDismiss = { showDialog = false },
                onConfirm = { producto ->
                    viewModel.agregarProducto(producto) { ok, _ ->
                        // cierre inmediato (y lista se actualizará sola por el listener)
                        showDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun ProductosSection(
    productos: List<Producto>,
    isLoading: Boolean,
    onAgregar: () -> Unit,
    onEliminar: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mis Productos", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = onAgregar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar")
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            productos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Sin productos",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay productos", fontWeight = FontWeight.SemiBold)
                        Text("Haz click en Agregar para crear uno", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productos) { p ->
                        ProductoItem(producto = p, onDelete = onEliminar)
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductoDialog(
    onDismiss: () -> Unit,
    onConfirm: (Producto) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Agregar Producto", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                error?.let { Text(it, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp)) }

                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre *") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombre.isBlank()) {
                                error = "El nombre es obligatorio"
                                return@Button
                            }
                            val prod = Producto(
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                precio = precio.toDoubleOrNull() ?: 0.0
                            )
                            onConfirm(prod)
                            // 🔔 Cierra el diálogo de inmediato para feedback instantáneo
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoItem(producto: Producto, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Text(producto.descripcion, color = Color.Gray)
                Text("Precio: $${producto.precio}", color = Color(0xFFFF9800))
            }
            IconButton(onClick = { producto.id?.let { onDelete(it) } }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}
