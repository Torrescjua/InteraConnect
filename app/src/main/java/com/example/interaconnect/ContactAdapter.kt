package com.example.interaconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(private val contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    // ViewHolder para almacenar las vistas del elemento de la lista
    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val contactNumber: TextView = itemView.findViewById(R.id.contactNumber)
        val contactImage: ImageView = itemView.findViewById(R.id.contactImage)
    }

    // Crea un nuevo ViewHolder cuando no haya suficientes ViewHolders para ser reciclados
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    // Enlaza los datos con el ViewHolder cuando se recicla
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.contactNumber.text = (position + 1).toString() // Mostrar el número del contacto
        holder.contactName.text = contact.name // Mostrar el nombre del contacto
        holder.contactImage.setImageResource(R.drawable.contacts) // Usar drawable del contacto
    }

    // Devuelve el número total de elementos de la lista
    override fun getItemCount(): Int {
        return contacts.size
    }
}
