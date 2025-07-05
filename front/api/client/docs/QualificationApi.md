# QualificationApi

All URIs are relative to *http://localhost:4011*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**deleteQualification**](#deletequalification) | **DELETE** /qualification/{qualificationId} | 資格情報削除(ID)|
|[**getQualification**](#getqualification) | **GET** /qualification | 資格情報取得|
|[**getQualificationById**](#getqualificationbyid) | **GET** /qualification/{qualificationId} | 資格情報取得(ID)|
|[**putQualification**](#putqualification) | **PUT** /qualification | 資格情報登録|

# **deleteQualification**
> deleteQualification()

IDを指定して資格情報を削除する

### Example

```typescript
import {
    QualificationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new QualificationApi(configuration);

let qualificationId: string; //資格ID (default to undefined)

const { status, data } = await apiInstance.deleteQualification(
    qualificationId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **qualificationId** | [**string**] | 資格ID | defaults to undefined|


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

# **getQualification**
> Array<Qualification> getQualification()

検索条件を指定して資格情報を取得する

### Example

```typescript
import {
    QualificationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new QualificationApi(configuration);

let qualificationName: string; //資格名 (optional) (default to undefined)
let status: Array<'dream' | 'planning' | 'acquired'>; //ステータス (optional) (default to undefined)
let rank: Array<'D' | 'C' | 'B' | 'A'>; //ランク (optional) (default to undefined)
let organization: string; //発行組織 (optional) (default to undefined)
let acquiredDateFrom: string; //取得年月日From (optional) (default to undefined)
let acquiredDateTo: string; //取得年月日To (optional) (default to undefined)
let expirationDateFrom: string; //有効期限From (optional) (default to undefined)
let expirationDateTo: string; //有効期限To (optional) (default to undefined)
let sortKey: 'qualificationName' | 'rank' | 'organization' | 'acquiredDate' | 'expirationDate'; //ソートキー (optional) (default to undefined)

const { status, data } = await apiInstance.getQualification(
    qualificationName,
    status,
    rank,
    organization,
    acquiredDateFrom,
    acquiredDateTo,
    expirationDateFrom,
    expirationDateTo,
    sortKey
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **qualificationName** | [**string**] | 資格名 | (optional) defaults to undefined|
| **status** | **Array<&#39;dream&#39; &#124; &#39;planning&#39; &#124; &#39;acquired&#39;>** | ステータス | (optional) defaults to undefined|
| **rank** | **Array<&#39;D&#39; &#124; &#39;C&#39; &#124; &#39;B&#39; &#124; &#39;A&#39;>** | ランク | (optional) defaults to undefined|
| **organization** | [**string**] | 発行組織 | (optional) defaults to undefined|
| **acquiredDateFrom** | [**string**] | 取得年月日From | (optional) defaults to undefined|
| **acquiredDateTo** | [**string**] | 取得年月日To | (optional) defaults to undefined|
| **expirationDateFrom** | [**string**] | 有効期限From | (optional) defaults to undefined|
| **expirationDateTo** | [**string**] | 有効期限To | (optional) defaults to undefined|
| **sortKey** | [**&#39;qualificationName&#39; | &#39;rank&#39; | &#39;organization&#39; | &#39;acquiredDate&#39; | &#39;expirationDate&#39;**]**Array<&#39;qualificationName&#39; &#124; &#39;rank&#39; &#124; &#39;organization&#39; &#124; &#39;acquiredDate&#39; &#124; &#39;expirationDate&#39;>** | ソートキー | (optional) defaults to undefined|


### Return type

**Array<Qualification>**

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

# **getQualificationById**
> QualificationId getQualificationById()

IDを指定して資格情報を取得する

### Example

```typescript
import {
    QualificationApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new QualificationApi(configuration);

let qualificationId: string; //資格ID (default to undefined)

const { status, data } = await apiInstance.getQualificationById(
    qualificationId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **qualificationId** | [**string**] | 資格ID | defaults to undefined|


### Return type

**QualificationId**

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

# **putQualification**
> QualificationId putQualification()

資格報を登録(登録済みの場合は情報を置き換え)する

### Example

```typescript
import {
    QualificationApi,
    Configuration,
    Qualification
} from './api';

const configuration = new Configuration();
const apiInstance = new QualificationApi(configuration);

let qualification: Qualification; // (optional)

const { status, data } = await apiInstance.putQualification(
    qualification
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **qualification** | **Qualification**|  | |


### Return type

**QualificationId**

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

