/*
 * Copyright (C) 2024 The Android Open Source Project
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

package android.service.autofill;

import android.annotation.NonNull;
import android.credentials.GetCredentialResponse;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.util.DataClass;


/**
 * This class represents a request to an autofill service to convert the credential manager response
 * to a dataset.
 *
 * @hide
 */
@DataClass(
        genToString = true,
        genHiddenConstructor = true,
        genHiddenConstDefs = true)
public final class ConvertCredentialRequest implements Parcelable {
    private final @NonNull GetCredentialResponse mGetCredentialResponse;
    private final @NonNull Bundle mClientState;



    // Code below generated by codegen v1.0.23.
    //
    // DO NOT MODIFY!
    // CHECKSTYLE:OFF Generated code
    //
    // To regenerate run:
    // $ codegen $ANDROID_BUILD_TOP/frameworks/base/core/java/android/service/autofill/ConvertCredentialRequest.java
    //
    // To exclude the generated code from IntelliJ auto-formatting enable (one-time):
    //   Settings > Editor > Code Style > Formatter Control
    //@formatter:off


    /**
     * Creates a new ConvertCredentialRequest.
     *
     * @hide
     */
    @DataClass.Generated.Member
    public ConvertCredentialRequest(
            @NonNull GetCredentialResponse getCredentialResponse,
            @NonNull Bundle clientState) {
        this.mGetCredentialResponse = getCredentialResponse;
        com.android.internal.util.AnnotationValidations.validate(
                NonNull.class, null, mGetCredentialResponse);
        this.mClientState = clientState;
        com.android.internal.util.AnnotationValidations.validate(
                NonNull.class, null, mClientState);

        // onConstructed(); // You can define this method to get a callback
    }

    @DataClass.Generated.Member
    public @NonNull GetCredentialResponse getGetCredentialResponse() {
        return mGetCredentialResponse;
    }

    @DataClass.Generated.Member
    public @NonNull Bundle getClientState() {
        return mClientState;
    }

    @Override
    @DataClass.Generated.Member
    public String toString() {
        // You can override field toString logic by defining methods like:
        // String fieldNameToString() { ... }

        return "ConvertCredentialRequest { " +
                "getCredentialResponse = " + mGetCredentialResponse + ", " +
                "clientState = " + mClientState +
        " }";
    }

    @Override
    @DataClass.Generated.Member
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // You can override field parcelling by defining methods like:
        // void parcelFieldName(Parcel dest, int flags) { ... }

        dest.writeTypedObject(mGetCredentialResponse, flags);
        dest.writeBundle(mClientState);
    }

    @Override
    @DataClass.Generated.Member
    public int describeContents() { return 0; }

    /** @hide */
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @DataClass.Generated.Member
    /* package-private */ ConvertCredentialRequest(@NonNull Parcel in) {
        // You can override field unparcelling by defining methods like:
        // static FieldType unparcelFieldName(Parcel in) { ... }

        GetCredentialResponse getCredentialResponse = (GetCredentialResponse) in.readTypedObject(GetCredentialResponse.CREATOR);
        Bundle clientState = in.readBundle();

        this.mGetCredentialResponse = getCredentialResponse;
        com.android.internal.util.AnnotationValidations.validate(
                NonNull.class, null, mGetCredentialResponse);
        this.mClientState = clientState;
        com.android.internal.util.AnnotationValidations.validate(
                NonNull.class, null, mClientState);

        // onConstructed(); // You can define this method to get a callback
    }

    @DataClass.Generated.Member
    public static final @NonNull Parcelable.Creator<ConvertCredentialRequest> CREATOR
            = new Parcelable.Creator<ConvertCredentialRequest>() {
        @Override
        public ConvertCredentialRequest[] newArray(int size) {
            return new ConvertCredentialRequest[size];
        }

        @Override
        public ConvertCredentialRequest createFromParcel(@NonNull Parcel in) {
            return new ConvertCredentialRequest(in);
        }
    };

    @DataClass.Generated(
            time = 1706132305002L,
            codegenVersion = "1.0.23",
            sourceFile = "frameworks/base/core/java/android/service/autofill/ConvertCredentialRequest.java",
            inputSignatures = "private final @android.annotation.NonNull android.credentials.GetCredentialResponse mGetCredentialResponse\nprivate final @android.annotation.NonNull android.os.Bundle mClientState\nclass ConvertCredentialRequest extends java.lang.Object implements [android.os.Parcelable]\n@com.android.internal.util.DataClass(genToString=true, genHiddenConstructor=true, genHiddenConstDefs=true)")
    @Deprecated
    private void __metadata() {}


    //@formatter:on
    // End of generated code

}