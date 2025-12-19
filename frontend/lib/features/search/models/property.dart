import 'package:json_annotation/json_annotation.dart';

@JsonSerializable()
class Property {
  final String id;
  final String name;
  final String? description;
  final String addressLine1;
  final String city;
  final String state;
  final String zipCode;
  final String? phone;
  final String? email;

  Property({
    required this.id,
    required this.name,
    this.description,
    required this.addressLine1,
    required this.city,
    required this.state,
    required this.zipCode,
    this.phone,
    this.email,
  });

  factory Property.fromJson(Map<String, dynamic> json) => Property(
    id: json['id'],
    name: json['name'],
    description: json['description'],
    addressLine1: json['addressLine1'],
    city: json['city'],
    state: json['state'],
    zipCode: json['zipCode'],
    phone: json['phone'],
    email: json['email'],
  );
}

class PropertySearchResult {
  final Property property;
  final int lowestPriceCents;
  final int availableRoomTypeCount;

  PropertySearchResult({
    required this.property,
    required this.lowestPriceCents,
    required this.availableRoomTypeCount,
  });

  factory PropertySearchResult.fromJson(Map<String, dynamic> json) => PropertySearchResult(
    property: Property.fromJson(json['property']),
    lowestPriceCents: json['lowestPriceCents'],
    availableRoomTypeCount: json['availableRoomTypeCount'],
  );
}
