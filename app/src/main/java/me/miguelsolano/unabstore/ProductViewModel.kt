package me.miguelsolano.unabstore

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // 🔹 Agregar producto
    fun agregarProducto(producto: Producto, onResult: (Boolean, String) -> Unit) {
        db.collection("Producto")
            .document("kkTwLwbXb5ZILszN5FCV")
            .collection("Producto")

            .add(producto)
            .addOnSuccessListener {
                onResult(true, "Producto agregado correctamente")
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Error desconocido")
            }
    }

    // 🔹 Escuchar productos en tiempo real
    fun observarProductosTiempoReal(onChange: (List<Producto>) -> Unit) {
        db.collection("Producto")
            .document("kkTwLwbXb5ZILszN5FCV")
            .collection("Producto")

            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onChange(lista)
            }
    }

    // 🔹 Eliminar producto
    fun eliminarProducto(id: String, onResult: (Boolean) -> Unit) {
        db.collection("Producto")
            .document("kkTwLwbXb5ZILszN5FCV")
            .collection("Producto")

            .document(id)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
