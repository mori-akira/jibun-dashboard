# Index1

給与情報

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**salaryId** | **string** | 給与ID | [optional] [default to undefined]
**userId** | **string** | ユーザID | [default to undefined]
**targetDate** | **string** | 対象年月日 | [default to undefined]
**overview** | [**Overview**](Overview.md) |  | [default to undefined]
**structure** | [**Structure**](Structure.md) |  | [default to undefined]
**payslipData** | [**Array&lt;PayslipData&gt;**](PayslipData.md) |  | [default to undefined]

## Example

```typescript
import { Index1 } from './api';

const instance: Index1 = {
    salaryId,
    userId,
    targetDate,
    overview,
    structure,
    payslipData,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
