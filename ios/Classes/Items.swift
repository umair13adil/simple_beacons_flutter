import Foundation
import CoreLocation

struct ItemConstant {
  static let nameKey = "name"
  static let uuidKey = "uuid"
  // static let majorKey = "major"
  // static let minorKey = "minor"
}

class Item: NSObject, NSCoding {
  let name: String
  let uuid: UUID
  // let majorValue: CLBeaconMajorValue
  // let minorValue: CLBeaconMinorValue
  var beacon: CLBeacon?
  
  // init(name: String,  uuid: UUID, majorValue: Int, minorValue: Int) {
  init(name: String,  uuid: UUID) {
    self.name = name
    self.uuid = uuid
    // self.majorValue = CLBeaconMajorValue(majorValue)
    // self.minorValue = CLBeaconMinorValue(minorValue)
  }

  // MARK: NSCoding
  required init(coder aDecoder: NSCoder) {
    let aName = aDecoder.decodeObject(forKey: ItemConstant.nameKey) as? String
    name = aName ?? ""
    
    let aUUID = aDecoder.decodeObject(forKey: ItemConstant.uuidKey) as? UUID
    uuid = aUUID ?? UUID()
    
    // majorValue = UInt16(aDecoder.decodeInteger(forKey: ItemConstant.majorKey))
    // minorValue = UInt16(aDecoder.decodeInteger(forKey: ItemConstant.minorKey))
  }
  
  func encode(with aCoder: NSCoder) {
    aCoder.encode(name, forKey: ItemConstant.nameKey)
    aCoder.encode(uuid, forKey: ItemConstant.uuidKey)
    // aCoder.encode(Int(majorValue), forKey: ItemConstant.majorKey)
    // aCoder.encode(Int(minorValue), forKey: ItemConstant.minorKey)
  }

  func asBeaconRegion() -> CLBeaconRegion {
    return CLBeaconRegion(proximityUUID: uuid,
                          // major: majorValue,
                          // minor: minorValue,
                          identifier: name)
  }
  
  func locationString() -> String {
    guard let beacon = beacon else { return "Location: Unknown" }
    let accuracy = String(format: "%.2f", beacon.accuracy)
    return "\(accuracy)"
  }
  
  func nameForProximity(_ proximity: CLProximity) -> String {
    switch proximity {
    case .unknown:
      return "Unknown"
    case .immediate:
      return "Immediate"
    case .near:
      return "Near"
    case .far:
      return "Far"
    @unknown default:
        return "Unknown"
    }
  }
  
}

func ==(item: Item, beacon: CLBeacon) -> Bool {
    return (beacon.proximityUUID.uuidString == item.uuid.uuidString)
        // && (Int(truncating: beacon.major) == Int(item.majorValue))
        // && (Int(truncating: beacon.minor) == Int(item.minorValue)))
}

