package com.example.sos.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sos.MyApplication
import com.example.sos.R
import com.example.sos.databinding.FragmentContactsBinding
import com.example.sos.databinding.SmsContactBinding
import com.example.sos.factory.MainViewModelFactory
import com.example.sos.utils.SharedPrefManager
import com.example.sos.utils.Utility
import com.example.sos.viewmodels.MainViewModel

class ContactsFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var mainViewModel: MainViewModel
    lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                MyApplication.firebaseCredential(requireContext()),
                MyApplication.getSharedPrefManager(requireContext())
            )
        )[MainViewModel::class.java]

        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        binding.tvSMSmessage.setOnClickListener(this)
        binding.tvEmailContact.setOnClickListener(this)
        binding.tvEmailMessage.setOnClickListener(this)
        binding.tvSMSContact.setOnClickListener(this)

        mainViewModel.resultIdLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                /** printing list with the help of Gson */
                /* val gson = Gson()
                 val stringData = gson.toJson(it)
                 Log.d("TAG", "long list : " + stringData)*/

                if (this::dialog.isInitialized && dialog.isShowing) {
                    dialog.dismiss()
                    //mainViewModel.contactListNew.postValue(null)
                    mainViewModel.addDummyContacts()
                    mainViewModel.addDummyMessage()
                }
                Toast.makeText(requireContext(), "Added", Toast.LENGTH_LONG).show()
                mainViewModel.resultIdLiveData.postValue(null)
            }
        })

        //Showing the data in EditText
        mainViewModel.listOfContact.observe(requireActivity(), Observer {
            if (it != null) {
                var list = it.toMutableList()
                mainViewModel.existInDatabase = true
                if ( list.size == 1 ) {
                    list.add(mainViewModel.contact2)
                    list.add(mainViewModel.contact3)
                } else if (list.size ==2) {
                    list.add(mainViewModel.contact3)
                }
                mainViewModel.contactListNew.postValue(list)
            }
        })

        mainViewModel.msgModel.observe(requireActivity(), Observer {
            if (it != null) {
                mainViewModel.existInDb = true
                mainViewModel.messageNew.postValue(it)
            }
        })

        return binding.root

    }

    private fun showAlertDialog(token: Int) {
        val smsDialogBinding: SmsContactBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.sms_contact,
            null,
            false
        )
        smsDialogBinding.viewmodel = mainViewModel
        smsDialogBinding.lifecycleOwner = viewLifecycleOwner

        when (token) {
            1 -> {
                // set token as sms
                val cList = mainViewModel.contactListNew.value
                cList?.forEach {
                    it.token = Utility.SMS_TOKEN
                }
                mainViewModel.contactListNew.postValue(cList)

                smsDialogBinding.smsContactLayout.visibility = View.VISIBLE
                smsDialogBinding.emailContactLayout.visibility = View.GONE
                smsDialogBinding.emailMessageLayout.visibility = View.GONE
                smsDialogBinding.smsMessageLayout.visibility = View.GONE

                mainViewModel.email.postValue(SharedPrefManager(requireContext()).getUserEmail())

                dialog = Dialog(requireContext())
                dialog.setCanceledOnTouchOutside(true)
                dialog.setContentView(smsDialogBinding.root)
                dialog.show()

                val window: Window? = dialog.getWindow()
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                smsDialogBinding.btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.setOnDismissListener {
                    mainViewModel.addDummyContacts()
                    mainViewModel.existInDatabase = false
                }

                mainViewModel.getContacts(SharedPrefManager(requireContext()).getUserEmail(), "sms")

            }
            2 -> {
                mainViewModel.messageNew.value?.token= Utility.SMS_TOKEN

                smsDialogBinding.smsContactLayout.visibility = View.GONE
                smsDialogBinding.emailContactLayout.visibility = View.GONE
                smsDialogBinding.emailMessageLayout.visibility = View.GONE
                smsDialogBinding.smsMessageLayout.visibility = View.VISIBLE

                dialog = Dialog(requireContext())
                dialog.setCanceledOnTouchOutside(true)
                dialog.setContentView(smsDialogBinding.root)
                dialog.show()

                val window: Window? = dialog.window
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                smsDialogBinding.btnSmsMessageCancel.setOnClickListener {
                    dialog.dismiss()
                }

                smsDialogBinding.btnClearSms.setOnClickListener {
                    smsDialogBinding.etSmsMessage.setText("")
                }

                dialog.setOnDismissListener {
                    mainViewModel.addDummyMessage()
                    mainViewModel.existInDb = false
                }

                mainViewModel.getMessage(SharedPrefManager(requireContext()).getUserEmail(), Utility.SMS_TOKEN)
            }
            3 -> {
                val cList = mainViewModel.contactListNew.value
                cList?.forEach {
                    it.token = Utility.EMAIL_TOKEN
                }

                mainViewModel.contactListNew.postValue(cList)

                smsDialogBinding.smsContactLayout.visibility = View.GONE
                smsDialogBinding.emailContactLayout.visibility = View.VISIBLE
                smsDialogBinding.emailMessageLayout.visibility = View.GONE
                smsDialogBinding.smsMessageLayout.visibility = View.GONE

                mainViewModel.email.postValue(SharedPrefManager(requireContext()).getUserEmail())

                dialog = Dialog(requireContext())
                dialog.setCanceledOnTouchOutside(true)
                dialog.setContentView(smsDialogBinding.root)
                dialog.show()

                //dialog Full window
                val window: Window? = dialog.window
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                //Fetch Contacts
                mainViewModel.getContacts(SharedPrefManager(requireContext()).getUserEmail(), Utility.EMAIL_TOKEN)

                dialog.setOnDismissListener {
                    mainViewModel.addDummyContacts()
                    mainViewModel.existInDatabase = false
                }

                smsDialogBinding.btnEmailCancel.setOnClickListener {
                    dialog.dismiss()
                }
            }
            4 -> {
                mainViewModel.messageNew.value?.token= Utility.EMAIL_TOKEN

                smsDialogBinding.smsContactLayout.visibility = View.GONE
                smsDialogBinding.emailContactLayout.visibility = View.GONE
                smsDialogBinding.emailMessageLayout.visibility = View.VISIBLE
                smsDialogBinding.smsMessageLayout.visibility = View.GONE

                dialog = Dialog(requireContext())
                dialog.setCanceledOnTouchOutside(true)
                dialog.setContentView(smsDialogBinding.root)
                dialog.show()

                val window: Window? = dialog.window
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                smsDialogBinding.btnEmailMessageCancel.setOnClickListener {
                    dialog.dismiss()
                }

                smsDialogBinding.btnClearEmail.setOnClickListener {
                    smsDialogBinding.etEmailContent.setText("")
                }

                dialog.setOnDismissListener {
                    mainViewModel.addDummyMessage()
                    mainViewModel.existInDb = false
                }

                mainViewModel.getMessage(SharedPrefManager(requireContext()).getUserEmail(), Utility.EMAIL_TOKEN)

            }
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvSMSContact ->
                showAlertDialog(1)
            R.id.tvSMSmessage ->
                showAlertDialog(2)
            R.id.tvEmailContact ->
                showAlertDialog(3)
            R.id.tvEmailMessage ->
                showAlertDialog(4)
        }
    }


}