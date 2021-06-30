package com.hanstech.mywallet

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import io.onebrick.sdk.*
import io.onebrick.sdk.model.*
import io.onebrick.sdk.util.Environment

class MainActivity : Activity(), ICoreBrickUISDK {

    private var coreSDK: CoreBrickUISDK? = null
    private var coreBrickUIDelegate: ICoreBrickUISDK? = null
    lateinit var txtHasil: TextView
    private var isInsData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        coreBrickUIDelegate = this as ICoreBrickUISDK
        txtHasil = findViewById<TextView>(R.id.txtHasil)
        initSdk()

        val btnAccessToken = findViewById<Button>(R.id.btnAccessToken)
        btnAccessToken.setOnClickListener {
            Log.d("HANSTECH", "Klik btnAccessToken");
            txtHasil.setText("")
            CoreBrickSDK.requestAccessToken(object : IAccessTokenRequestResult {
                override fun success(accessToken: AccessToken?) {
                    Log.d("HANSTECH", accessToken.toString())
                    txtHasil.setText(accessToken.toString())
                }
                override fun error(t: Throwable?) {
                    Log.d("HANSTECH", "Error btnAccessToken: " + t.toString())
                    txtHasil.setText("Error btnAccessToken: " + t.toString())
                }
            })
        }

        val btnAccessTokenCredential = findViewById<Button>(R.id.btnAccessTokenCredential)
        btnAccessTokenCredential.setOnClickListener {
            Log.d("HANSTECH", "Klik btnAccessTokenCredential");
            txtHasil.setText("")
            CoreBrickSDK.requestTokenCredentials(object: IRequestTokenCredentials {
                override fun success(accessTokenRequest: AccessTokenRequest?) {
                    Log.d("HANSTECH", accessTokenRequest.toString())
                    txtHasil.setText(accessTokenRequest.toString())
                }

                override fun error(t: Throwable?) {
                    Log.d("HANSTECH", "Error btnAccessTokenCredential: " + t.toString())
                    txtHasil.setText("Error btnAccessTokenCredential: " + t.toString())
                }
            })
        }

        val btnListInstitution = findViewById<Button>(R.id.btnListInstitution)
        btnListInstitution.setOnClickListener {
            Log.d("HANSTECH", "Klik btnListInstitution");
            txtHasil.setText("")
            CoreBrickSDK.listInstitution(object: IRequestInstituion {
                override fun success(institution: Institution?) {
                    Log.d("HANSTECH", institution.toString())
                    txtHasil.setText(institution.toString())
                }

                override fun error(t: Throwable?) {
                    Log.d("HANSTECH", "Error btnListInstitution: " + t.toString())
                    txtHasil.setText("Error btnListInstitution: " + t.toString())
                }
            })
        }

        val btnSubmitInstitution = findViewById<Button>(R.id.btnSubmitInstitution)
        btnSubmitInstitution.setOnClickListener {
            Log.d("HANSTECH", "Klik btnSubmitInstitution");
            txtHasil.setText("")
            CoreBrickSDK.submitInstitution(BuildConfig.NAME, "Internet Banking", object: IRequestSubmitInstitution {
                override fun success(institutionResponseSubmit: InstitutionResponseSubmit?) {
                    Log.d("HANSTECH", institutionResponseSubmit.toString())
                    txtHasil.setText(institutionResponseSubmit.toString())
                }

                override fun error(t: Throwable?) {
                    Log.d("HANSTECH", "Error btnSubmitInstitution: " + t.toString())
                    txtHasil.setText("Error btnSubmitInstitution: " + t.toString())
                }
            })
        }

        val btnAuthUser = findViewById<Button>(R.id.btnAuthUser)
        btnAuthUser.setOnClickListener {
            val username = "johndoe"
            val password = "fintechFirst"
            val institutionId = "7"

            Log.d("HANSTECH", "Klik btnAuthUser");
            txtHasil.setText("")

            for(item in ConfigStorage.institutionList.data) {
                if(item.id.toString() == institutionId) {
                    ConfigStorage.setCurrentInstitution(item)
//                    ConfigStorage.institutionData = item
                    Log.d("HANSTECH", ConfigStorage.institutionData.toString());
                    isInsData = true;
                }
            }

            CoreBrickSDK.authenticateUser(username, password, institutionId, object: IRequestResponseUserAuth {
                override fun success(authenticateUserResponse: AuthenticateUserResponse) {
                    Log.d("HANSTECH", authenticateUserResponse.toString())
                    txtHasil.setText(authenticateUserResponse.toString())

                    //cek status
                    var status: Long? = authenticateUserResponse.status
                    if(status.toString() == "428") {
                        //hit submitCredentialsForMFAAccount
                        Log.d("HANSTECH", "Klik submitCredentialsForMFAAccount");

                        val payload = MFABankingPayload(
                            token = "150312",
                            duration = 30,
                            institutionId = institutionId.toLong(),
                            redirectRefId = ConfigStorage.redirectReffId,
                            sessionId = ConfigStorage.userSessionToken,
                            username = username,
                            password = password
                        )

                        CoreBrickSDK.submitCredentialsForMFAAccount(payload, object: IRequestTransactionResult{
                            override fun success(authenticateUserResponse: AuthenticateUserResponse?) {
                                if (authenticateUserResponse != null) {
                                    txtHasil.setText(authenticateUserResponse.toString())
                                    Log.d("HANSTECH", authenticateUserResponse.toString())
                                    ConfigStorage.userSessionToken = authenticateUserResponse.data?.accessToken.toString()
                                    Log.d("HANSTECH", ConfigStorage.userSessionToken)
                                }
                            }

                            override fun error(t: Throwable?) {
                                Log.d("HANSTECH", "Error submitCredentialsForMFAAccount: " + t.toString())
                                txtHasil.setText("Error submitCredentialsForMFAAccount: " + t.toString())
                            }
                        })
                    } else {
                        ConfigStorage.userSessionToken = authenticateUserResponse.data?.accessToken.toString()
                        Log.d("HANSTECH", ConfigStorage.userSessionToken)
                    }
                }
                override fun error(t: Throwable?) {
                    Log.d("HANSTECH", "Error btnAuthUser: " + t.toString())
                    txtHasil.setText("Error btnAuthUser: " + t.toString())
                }
            })
        }

        val btnResendOtp = findViewById<Button>(R.id.btnResendOtp)
        btnResendOtp.setOnClickListener {
            txtHasil.setText("")
            if(isInsData) {
                Log.d("HANSTECH", "Klik btnResendOtp");

                CoreBrickSDK.requestResendOTP(object: IRequestTransactionResult {
                    override fun success(authenticateUserResponse: AuthenticateUserResponse?) {
                        if (authenticateUserResponse != null) {
                            Log.d("HANSTECH", authenticateUserResponse.toString())
                            txtHasil.setText(authenticateUserResponse.toString())
                            ConfigStorage.userSessionToken = authenticateUserResponse.data?.accessToken.toString()
                            Log.d("HANSTECH", ConfigStorage.userSessionToken)
                        }
                    }

                    override fun error(t: Throwable?) {
                        Log.d("HANSTECH", "Error btnResendOtp: " + t.toString())
                        txtHasil.setText("Error btnResendOtp: " + t.toString())
                    }
                })
            } else {
                Log.d("HANSTECH", "Error btnResendOtp: Auth User terlebih dahulu")
                txtHasil.setText("Error btnResendOtp: Auth User terlebih dahulu")
            }
        }

        val btnAuthEwalletUser = findViewById<Button>(R.id.btnAuthEwalletUser)
        btnAuthEwalletUser.setOnClickListener {
            Log.d("HANSTECH", "Klik btnAuthEwalletUser");

            var username = "085214056793"
            var password = ""
            var institutionId = "11"

            val payload = MFABankingPayload(
                username = username,
                institutionId = institutionId.toLong(),
                redirectRefId = ConfigStorage.redirectReffId
            )

            for(item in ConfigStorage.institutionList.data) {
                if(item.id.toString() == institutionId) {
                    ConfigStorage.setCurrentInstitution(item)
//                    ConfigStorage.institutionData = item
                    Log.d("HANSTECH", ConfigStorage.institutionData.toString());
                    isInsData = true;
                }
            }

            txtHasil.setText("")
            CoreBrickSDK.authenticateEwalletUser(payload, object: IRequestTransactionResult {
                override fun success(authenticateUserResponse: AuthenticateUserResponse?) {
                    if (authenticateUserResponse != null) {
                        Log.d("HANSTECH", authenticateUserResponse.toString())
                        txtHasil.setText(authenticateUserResponse.toString())

                        //cek status
                        var status: Long? = authenticateUserResponse.status
                        if(status.toString() == "428") {
                            //hit submitCredentialsForMFAAccount
                            Log.d("HANSTECH", "Klik submitCredentialsForMFAAccount");

                            val payload = MFABankingPayload(
                                token = "3318",
                                duration = 30,
                                institutionId = institutionId.toLong(),
                                redirectRefId = ConfigStorage.redirectReffId,
                                sessionId = ConfigStorage.userSessionToken,
                                username = username,
                                password = password
                            )

                            CoreBrickSDK.submitCredentialsForMFAAccount(payload, object: IRequestTransactionResult{
                                override fun success(authenticateUserResponse: AuthenticateUserResponse?) {
                                    if (authenticateUserResponse != null) {
                                        Log.d("HANSTECH", authenticateUserResponse.toString())
                                        txtHasil.setText(authenticateUserResponse.toString())
                                        ConfigStorage.userSessionToken = authenticateUserResponse.data?.accessToken.toString()
                                        Log.d("HANSTECH", ConfigStorage.userSessionToken)
                                    }
                                }

                                override fun error(t: Throwable?) {
                                    Log.d("HANSTECH", "Error submitCredentialsForMFAAccount: " + t.toString())
                                    txtHasil.setText("Error submitCredentialsForMFAAccount: " + t.toString())
                                }
                            })
                        } else {
                            ConfigStorage.userSessionToken = authenticateUserResponse.data?.accessToken.toString()
                            Log.d("HANSTECH", ConfigStorage.userSessionToken)
                        }
                    }
                }

                override fun error(t: Throwable?) {
                    Log.d("HANSTECH", "Error btnAuthEwalletUser: " + t.toString())
                    txtHasil.setText("Error btnAuthEwalletUser: " + t.toString())
                }
            })
        }

        val btnListAccount = findViewById<Button>(R.id.btnListAccount)
        btnListAccount.setOnClickListener {
            Log.d("HANSTECH", "Klik btnListAccount");
            txtHasil.setText("")
            CoreBrickSDK.listAccountUser()
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            CoreBrickUISDK.initializedUISDK( applicationContext, BuildConfig.CLIENTID, BuildConfig.CLIENTSECRET, BuildConfig.NAME, BuildConfig.URL,
                this.coreBrickUIDelegate!!, Environment.SANDBOX)
        }
    }

    override fun onTransactionSuccess(transactionResult: AuthenticateUserResponseData) {
        Log.d("HANSTECH", transactionResult.toString())
        txtHasil.setText(transactionResult.toString())
        ConfigStorage.userSessionToken = transactionResult.accessToken.toString();
    }

    fun initSdk() {
        Log.d("HANSTECH", "Init SDK");
        CoreBrickSDK.initializedSDK(BuildConfig.CLIENTID, BuildConfig.CLIENTSECRET, BuildConfig.NAME, BuildConfig.URL, Environment.SANDBOX)
    }
}