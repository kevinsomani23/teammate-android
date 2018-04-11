package com.mainstreetcode.teammate.model;

import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.persistence.entity.UserEntity;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static com.mainstreetcode.teammate.util.ModelUtils.asString;

public class User extends UserEntity implements
        Model<User>,
        HeaderedModel<User>,
        ItemListableBean<User> {

    public static final String PHOTO_UPLOAD_KEY = "user-photo";

    @Ignore private transient String password;
    @Ignore private final List<Item<User>> items;

    public User(String id, String firstName, String lastName, String primaryEmail, String about, String imageUrl) {
        super(id, firstName, lastName, primaryEmail, about, imageUrl);

        items = buildItems();
    }

    protected User(Parcel in) {
        super(in);
        items = buildItems();
    }

    public static User empty() {
        return new User("", "", "", "", "", Config.getDefaultUserAvatar());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Item<User>> buildItems() {
        return Arrays.asList(
                Item.text(Item.INPUT, R.string.first_name, firstName == null ? "" : firstName, this::setFirstName, this),
                Item.text(Item.INPUT, R.string.last_name, lastName == null ? "" : lastName, this::setLastName, this),
                Item.email(Item.INPUT, R.string.email, primaryEmail == null ? "" : primaryEmail, this::setPrimaryEmail, this),
                Item.text(Item.INPUT, R.string.user_about, about == null ? "" : about, this::setAbout, this)
        );
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Item get(int position) {
        return items.get(position);
    }

    @Override
    public Item<User> getHeaderItem() {
        return Item.text(Item.IMAGE, R.string.profile_picture, imageUrl, this::setImageUrl, this);
    }

    @Override
    public boolean areContentsTheSame(Identifiable other) {
        if (!(other instanceof User)) return id.equals(other.getId());
        User casted = (User) other;
        return firstName.equals(casted.getFirstName()) && lastName.equals(casted.getLastName())
                && imageUrl.equals(casted.getImageUrl());
    }

    @Override
    public Object getChangePayload(Identifiable other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return TextUtils.isEmpty(id);
    }

    @Override
    public void reset() {
        firstName = "";
        lastName = "";
        primaryEmail = "";
        imageUrl = "";

        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue("");
    }

    @Override
    public void update(User updatedUser) {
        this.id = updatedUser.id;
        this.imageUrl = updatedUser.imageUrl;

        int size = size();
        for (int i = 0; i < size; i++) get(i).setValue(updatedUser.get(i).getValue());
    }

    @Override
    public int compareTo(@NonNull User o) {
        int firstNameComparison = firstName.compareTo(o.firstName);
        int lastNameComparison = lastName.compareTo(o.lastName);

        return firstNameComparison != 0
                ? firstNameComparison
                : lastNameComparison != 0
                ? lastNameComparison
                : id.compareTo(o.id);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static class GsonAdapter implements
            JsonSerializer<User>,
            JsonDeserializer<User> {

        private static final String UID_KEY = "_id";
        private static final String LAST_NAME_KEY = "lastName";
        private static final String FIRST_NAME_KEY = "firstName";
        private static final String IMAGE_KEY = "imageUrl";
        private static final String PRIMARY_EMAIL_KEY = "primaryEmail";
        private static final String ABOUT_KEY = "about";
        private static final String PASSWORD_KEY = "password";

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new User(json.getAsString(), "", "", "", "", "");
            }

            JsonObject userObject = json.getAsJsonObject();

            String id = asString(UID_KEY, userObject);
            String firstName = asString(FIRST_NAME_KEY, userObject);
            String lastName = asString(LAST_NAME_KEY, userObject);
            String primaryEmail = asString(PRIMARY_EMAIL_KEY, userObject);
            String about = asString(ABOUT_KEY, userObject);
            String imageUrl = asString(IMAGE_KEY, userObject);

            return new User(id, firstName, lastName, primaryEmail, about, imageUrl);
        }

        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject user = new JsonObject();
            user.addProperty(FIRST_NAME_KEY, src.firstName);
            user.addProperty(LAST_NAME_KEY, src.lastName);
            user.addProperty(PRIMARY_EMAIL_KEY, src.primaryEmail);
            user.addProperty(ABOUT_KEY, src.about);

            if (!TextUtils.isEmpty(src.password)) user.addProperty(PASSWORD_KEY, src.password);

            return user;
        }
    }
}
