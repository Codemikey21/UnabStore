package me.miguelsolano.unabstore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: FirebaseUser?,
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var productos by remember { mutableStateOf(listOf<Producto>()) }
    var showDialog by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    // 🔄 Cargar productos al abrir
    LaunchedEffect(Unit) {
        productViewModel.obtenerProductos { productos = it }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFFFF9800)) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto", tint = Color.White)
            }
        },
        topBar = {
            SmallTopAppBar(
                title = { Text("Unab Shop", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFFFF9800))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Perfil del usuario ---
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { authViewModel.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Cerrar sesión", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Productos disponibles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            // --- Lista de productos ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos) { producto ->
                    ProductCard(
                        producto = producto,
                        onDelete = {
                            scope.launch {
                                productViewModel.eliminarProducto(producto.id ?: "") { ok ->
                                    if (ok) {
                                        productViewModel.obtenerProductos { productos = it }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // --- Diálogo para agregar producto ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Agregar nuevo producto") },
                text = {
                    Column {
                        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                        OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val nuevo = Producto(
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio.toDoubleOrNull() ?: 0.0
                        )
                        productViewModel.agregarProducto(nuevo) { ok, _ ->
                            if (ok) {
                                productViewModel.obtenerProductos { productos = it }
                            }
                        }
                        showDialog = false
                        nombre = ""
                        descripcion = ""
                        precio = ""
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
                        Text("Guardar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductCard(producto: Producto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("Precio: $${producto.precio}", fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9800))
            }
            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}
