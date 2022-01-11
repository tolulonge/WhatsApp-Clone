package com.tolulonge.whatsappclone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.firebase.database.*
import com.tolulonge.whatsappclone.databinding.FragmentGroupsBinding

class GroupsFragment : Fragment() {


    private var _binding: FragmentGroupsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var arrayAdapter : ArrayAdapter<String>? = null
    private var listOfGroups = arrayListOf<String>()
    private var groupRef : DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        groupRef = FirebaseDatabase.getInstance().reference.child("Groups")

        retrieveAndDisplayGroups()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrayAdapter =
            context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, listOfGroups) }
        binding.listView.adapter = arrayAdapter
    }

    private fun retrieveAndDisplayGroups() {
        groupRef?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val set = HashSet<String>()
                val iterator = snapshot.children.iterator()

                while (iterator.hasNext()){
                    set.add(((iterator.next()).key.toString()))
                }
                listOfGroups.clear()
                listOfGroups.addAll(set)
                arrayAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}