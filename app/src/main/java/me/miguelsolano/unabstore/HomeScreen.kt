package me.miguelsolano.unabstore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val vm: ProductViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var productos by remember { mutableStateOf(listOf<Producto>()) }
    var showDialog by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    // 🔄 Obtener productos cuando entra al Home
    LaunchedEffect(Unit) {
        vm.obtenerProductos {
            productos = it
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lista de Productos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {
                items(productos) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                                Text(producto.descripcion)
                                Text("Precio: $${producto.precio}")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    vm.eliminarProducto(producto.id ?: "") { ok ->
                                        if (ok) {
                                            vm.obtenerProductos { productos = it }
                                        }
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }

        // 🧾 Diálogo para agregar producto
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Agregar Producto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre") }
                        )
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") }
                        )
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it },
                            label = { Text("Precio") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (nombre.isNotEmpty() && precio.isNotEmpty()) {
                            val nuevo = Producto(
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precio.toDoubleOrNull() ?: 0.0
                            )
                            vm.agregarProducto(nuevo) { ok, _ ->
                                if (ok) {
                                    vm.obtenerProductos { productos = it }
                                }
                            }
                            showDialog = false
                            nombre = ""
                            descripcion = ""
                            precio = ""
                        }
                    }) {
                        Text("Guardar")
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
