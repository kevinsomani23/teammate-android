/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.model

import android.annotation.SuppressLint
import android.os.Parcel
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mainstreetcode.teammate.model.enums.Position
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.tunjid.androidx.recyclerview.diff.Differentiable
import java.lang.reflect.Type
import java.util.*

@SuppressLint("ParcelCreator")
class TeamMember internal constructor(val wrappedModel: TeamMemberModel<*>) : UserHost, TeamHost, Model<TeamMember> {

    override val id: String
        get() = wrappedModel.diffId

    override val user: User
        get() = wrappedModel.user

    override val team: Team
        get() = wrappedModel.team

    override val isEmpty: Boolean
        get() = wrappedModel.isEmpty

    override val imageUrl: String
        get() = wrappedModel.imageUrl

    val created: Date
        get() = when (wrappedModel) {
            is Role -> wrappedModel.created
            is JoinRequest -> wrappedModel.created
            else -> Date()
        }

    private val teamRole: Position
        get() = when (wrappedModel) {
            is Role -> wrappedModel.position
            is JoinRequest -> wrappedModel.position
            else -> Position.empty()
        }

    override fun compareTo(other: TeamMember): Int =
            FunctionalDiff.COMPARATOR.compare(wrappedModel, other)

    override fun update(updated: TeamMember) {}

    override fun areContentsTheSame(other: Differentiable): Boolean = when (other) {
        is TeamMember -> user.areContentsTheSame(other.user) && teamRole == other.teamRole
        else -> diffId == other.diffId
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is TeamMember -> false
        else -> wrappedModel == other.wrappedModel
    }

    override fun hashCode(): Int = wrappedModel.hashCode()

    override fun describeContents(): Int =
            throw IllegalArgumentException("TeamMember instances are not Parcelable")

    override fun writeToParcel(dest: Parcel, flags: Int): Unit =
            throw IllegalArgumentException("TeamMember instances are not Parcelable")

    class GsonAdapter : JsonDeserializer<TeamMember> {

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TeamMember {
            val jsonObject = json.asJsonObject
            val isJoinRequest = jsonObject.has(NAME_KEY) && jsonObject.get(NAME_KEY).isJsonPrimitive

            return when {
                isJoinRequest -> context.deserialize<JoinRequest>(jsonObject, JoinRequest::class.java).toTeamMember()
                else -> context.deserialize<Role>(jsonObject, Role::class.java).toTeamMember()
            }
        }

        companion object {

            private const val NAME_KEY = "roleName"
        }
    }

}

fun List<TeamMember>.split(listBiConsumer: (List<Role>, List<JoinRequest>) -> Unit) {
    val unwrapped = map { it.wrappedModel }
    val roles = unwrapped.filterIsInstance(Role::class.java)
    val requests = unwrapped.filterIsInstance(JoinRequest::class.java)

    listBiConsumer.invoke(roles, requests)
}