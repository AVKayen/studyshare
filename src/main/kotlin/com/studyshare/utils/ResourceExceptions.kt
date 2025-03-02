package com.studyshare.utils

class ResourceNotFoundException(): Exception("Resource not found")

class ResourceModificationRestrictedException(): Exception(
    "Resource Modification Restricted - Ownership Required"
)