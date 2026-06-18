/*
 * Copyright (C) 2026 The uwuAOSP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package moe.reimu.catshare.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import java.net.NetworkInterface
import kotlin.collections.iterator

object MacAddressUtils {
    fun getMacAddress(context: Context, name: String, l: (String?) -> Unit) {
        if (context.checkSelfPermission("android.permission.LOCAL_MAC_ADDRESS") != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "LOCAL_MAC_ADDRESS permission not granted")
            l(null)
            return
        }

        l(getMacAddressByName(name))
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getMacAddressByName(name: String): String? {
        val ifs = NetworkInterface.getNetworkInterfaces()
        for (intf in ifs) {
            if (intf.name == name) {
                return intf.hardwareAddress?.toHexString(HexFormat {
                    bytes.byteSeparator = ":"
                })
            }
        }
        return null
    }

    suspend fun getMacAddress(context: Context, name: String): String? {
        val fut = CompletableDeferred<String?>()
        getMacAddress(context, name) {
            fut.complete(it)
        }
        return fut.await()
    }
}
