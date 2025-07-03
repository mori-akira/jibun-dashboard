# UserApi

All URIs are relative to *http://localhost:4011*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getUser**](#getuser) | **GET** /user | ユーザ情報取得|
|[**postPassword**](#postpassword) | **POST** /user/password | パスワード更新|
|[**putUser**](#putuser) | **PUT** /user | ユーザ情報登録|

# **getUser**
> User getUser()

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

**User**

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

# **postPassword**
> postPassword()

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

const { status, data } = await apiInstance.postPassword(
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
    Configuration,
    User
} from './api';

const configuration = new Configuration();
const apiInstance = new UserApi(configuration);

let user: User; // (optional)

const { status, data } = await apiInstance.putUser(
    user
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **user** | **User**|  | |


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

