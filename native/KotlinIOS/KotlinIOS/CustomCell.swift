import UIKit

class CustomCell: UITableViewCell {
    
 
    @IBOutlet private var departureValue: UILabel!
    
    @IBOutlet private var arrivalValue: UILabel!
    
    @IBOutlet private var costValue: UILabel!
    
    func departureSet(message: String) {
        departureValue.text = message
        
    }
    
    func arrivalSet(message: String) {
        arrivalValue.text = message
        
    }
    
    func costSet(message: Int) {
        let currencyFormatter = NumberFormatter()
        currencyFormatter.usesGroupingSeparator = true
        currencyFormatter.numberStyle = .currency
        currencyFormatter.locale = Locale.init(identifier: "en_GB")
        let priceString = currencyFormatter.string(from: NSNumber(value: Double(message)/100.0))!
        costValue.text = priceString
    }
    
}
