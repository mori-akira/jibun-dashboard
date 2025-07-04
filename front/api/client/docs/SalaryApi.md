# SalaryApi

All URIs are relative to *http://localhost:4011*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**deleteSalary**](#deletesalary) | **DELETE** /salary/{salaryId} | 給与情報削除(ID)|
|[**getSalary**](#getsalary) | **GET** /salary | 給与情報取得|
|[**getSalaryById**](#getsalarybyid) | **GET** /salary/{salaryId} | 給与情報取得(ID)|
|[**putSalary**](#putsalary) | **PUT** /salary | 給与情報登録|

# **deleteSalary**
> deleteSalary()

IDを指定して給与情報を削除する

### Example

```typescript
import {
    SalaryApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SalaryApi(configuration);

let salaryId: string; //給与ID (default to undefined)

const { status, data } = await apiInstance.deleteSalary(
    salaryId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **salaryId** | [**string**] | 給与ID | defaults to undefined|


### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | 正常時 |  -  |
|**400** | パラメータ不正 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSalary**
> Array<Salary> getSalary()

検索条件を指定して給与情報を取得する

### Example

```typescript
import {
    SalaryApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SalaryApi(configuration);

let targetDate: string; //対象年月日 (optional) (default to undefined)
let targetDateFrom: string; //対象年月日From (optional) (default to undefined)
let targetDateTo: string; //対象年月日To (optional) (default to undefined)

const { status, data } = await apiInstance.getSalary(
    targetDate,
    targetDateFrom,
    targetDateTo
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **targetDate** | [**string**] | 対象年月日 | (optional) defaults to undefined|
| **targetDateFrom** | [**string**] | 対象年月日From | (optional) defaults to undefined|
| **targetDateTo** | [**string**] | 対象年月日To | (optional) defaults to undefined|


### Return type

**Array<Salary>**

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

# **getSalaryById**
> SalaryId getSalaryById()

IDを指定して給与情報を取得する

### Example

```typescript
import {
    SalaryApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new SalaryApi(configuration);

let salaryId: string; //給与ID (default to undefined)

const { status, data } = await apiInstance.getSalaryById(
    salaryId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **salaryId** | [**string**] | 給与ID | defaults to undefined|


### Return type

**SalaryId**

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

# **putSalary**
> SalaryId putSalary()

給与情報を登録(登録済みの場合は情報を置き換え)する

### Example

```typescript
import {
    SalaryApi,
    Configuration,
    Salary
} from './api';

const configuration = new Configuration();
const apiInstance = new SalaryApi(configuration);

let salary: Salary; // (optional)

const { status, data } = await apiInstance.putSalary(
    salary
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **salary** | **Salary**|  | |


### Return type

**SalaryId**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | 正常時 |  -  |
|**400** | パラメータ不正 |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

