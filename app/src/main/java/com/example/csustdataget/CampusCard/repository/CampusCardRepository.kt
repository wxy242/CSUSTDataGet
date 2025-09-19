package com.example.csustdataget.CampusCard.repository

import com.example.csustdataget.CampusCard.api.ElectronicApi
import com.example.csustdataget.core.RetrofitUtils

class CampusCardRepository private constructor() {
    companion object {
        val instance by lazy { CampusCardRepository() }
        private const val TAG = "CampusCardRepository"
        private const val GET_ROOM_INFO = "synjones.onecard.query.elec.roominfo"
    }

    private val elecApi by lazy { RetrofitUtils.instanceQueryElec.create(ElectronicApi::class.java) }
    suspend fun getElectricity(json: String): String {
        val response = elecApi.getElectricity(
            jsonData = json,
            funName = GET_ROOM_INFO
        )
        return response.toString()
    }
}