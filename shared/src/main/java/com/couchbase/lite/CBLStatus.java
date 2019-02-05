//
// CBLStatus.java
//
// Copyright (c) 2017 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.lite;

import com.couchbase.lite.internal.support.Log;
import com.couchbase.lite.internal.C4Base;
import com.couchbase.lite.internal.C4Constants;
import com.couchbase.lite.internal.C4Error;
import com.couchbase.lite.internal.LiteCoreException;

class CBLStatus {
    final static String[] kErrorDomains = {
            null,
            CBLError.Domain.CBLErrorDomain,     // LiteCoreDomain
            "POSIXErrorDomain",                 // POSIXDomain
            CBLError.Domain.SQLiteErrorDomain,  // SQLiteDomain
            CBLError.Domain.FleeceErrorDomain,  // FleeceDomain
            CBLError.Domain.CBLErrorDomain,     // Network error
            CBLError.Domain.CBLErrorDomain};    // WebSocketDomain

    static CouchbaseLiteException convertException(int domainCode, int statusCode, String message, LiteCoreException e) {
        String domain = kErrorDomains[domainCode];
        int code = statusCode;
        if (domainCode == C4Constants.C4ErrorDomain.NetworkDomain)
            code += CBLError.Code.CBLErrorNetworkBase;
        else if (domainCode == C4Constants.C4ErrorDomain.WebSocketDomain)
            code += CBLError.Code.CBLErrorHTTPBase;

        if (domain == null) {
            Log.w(Log.DATABASE, "Unable to map C4Error(%d,%d) to an CouchbaseLiteException", domainCode, statusCode);
            domain = CBLError.Domain.CBLErrorDomain;
            code = CBLError.Code.CBLErrorUnexpectedError;
        }

        message = message != null ? message : (e != null ? e.getMessage() : null);
        return new CouchbaseLiteException(message, e, domain, code);
    }

    static CouchbaseLiteException convertException(LiteCoreException e) {
        return convertException(e.domain, e.code, null, e);
    }

    static CouchbaseLiteException convertException(int domainCode, int statusCode, int _internalInfo) {
        if (domainCode != 0 && statusCode != 0) {
            String errMsg = C4Base.getMessage(domainCode, statusCode, _internalInfo);
            return convertException(new LiteCoreException(domainCode, statusCode, errMsg));
        } else
            return convertException(domainCode, statusCode, null, null);
    }

    static CouchbaseLiteException convertError(C4Error c4err) {
        return convertException(c4err.getDomain(), c4err.getCode(), c4err.getInternalInfo());
    }
}
