# UserApi

All URIs are relative to *http://localhost:4011/api/v1*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getUser**](#getuser) | **GET** /user | ユーザ情報取得|
|[**putPassword**](#putpassword) | **POST** /user/password | パスワード更新|
|[**putUser**](#putuser) | **PUT** /user | ユーザ情報登録|

# **getUser**
> Index getUser()

アクセストークンを用いて、現在ログイン中のユーザ情報を取得する

### Example

```typescript
import {
    UserApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new UserApi(configuration);

const { status, data } = await apiInstance.getUser();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Index**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | 正常時 |  -  |
|**400** | パラメータ不正 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putPassword**
> putPassword()

パスワード情報を更新する

### Example

```typescript
import {
    UserApi,
    Configuration,
    Password
} from './api';

const configuration = new Configuration();
const apiInstance = new UserApi(configuration);

let password: Password; // (optional)

const { status, data } = await apiInstance.putPassword(
    password
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **password** | **Password**|  | |


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 正常時 |  -  |
|**400** | パラメータ不正 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **putUser**
> putUser()

アクセストークンを用いて、現在ログイン中のユーザ情報を登録(登録済みの場合は情報を置き換え)する

### Example

```typescript
import {
    UserApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new UserApi(configuration);

let body: Index; // (optional)

const { status, data } = await apiInstance.putUser(
    body
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **body** | **Index**|  | |


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 正常時 |  -  |
|**400** | パラメータ不正 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

