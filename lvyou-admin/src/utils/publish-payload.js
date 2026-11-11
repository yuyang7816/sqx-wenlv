/** 提交前剥离手动选择的目的地与临时 UI 字段；坐标须来自地图选点 */
export async function withPublishLocation(payload) {
  const next = { ...payload }
  delete next.destinationId
  delete next.destinationName
  delete next._meetLatitude
  delete next._meetLongitude
  delete next._tagsText
  delete next._servicesText
  delete next._highlights
  delete next._noticesText
  delete next._tipsText
  delete next._itineraryDays
  delete next._locationSanitized
  delete next._editorPreparedForKey
  delete next._stationRows
  delete next._scheduleRows
  delete next._supplementLoaded
  if (next.latitude != null && next.latitude !== '') {
    next.latitude = Number(next.latitude)
  }
  if (next.longitude != null && next.longitude !== '') {
    next.longitude = Number(next.longitude)
  }
  return next
}

/** 包装已有 buildPayload，统一处理目的地与定位 */
export function wrapPublishPayload(buildPayload) {
  return async (form, editingId) => {
    const base = buildPayload ? await buildPayload(form, editingId) : { ...form }
    return withPublishLocation(base)
  }
}
