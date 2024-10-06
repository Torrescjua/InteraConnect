package com.example.interaconnect

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast

class ContactActivity : AppCompatActivity() {

    private lateinit var contactsRecyclerView: RecyclerView
    private val contactsList = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        initializeViews()
        checkContactsPermission()
    }

    // Inicializar vistas
    private fun initializeViews() {
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Verificar y solicitar permiso para leer contactos
    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
        } else {
            requestContactsPermission()
        }
    }

    // Solicitar permiso para leer contactos
    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            1
        )
    }

    // Obtener el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            showPermissionDeniedMessage()
        }
    }

    // Mostrar mensaje de permiso denegado
    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    // Método para cargar los contactos
    private fun loadContacts() {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )

        cursor?.let {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

            if (nameIndex != -1) {
                while (it.moveToNext()) {
                    val name = it.getString(nameIndex)
                    contactsList.add(Contact(name))
                }
            }
            it.close()
        }

        contactsRecyclerView.adapter = ContactAdapter(contactsList)
    }
}
