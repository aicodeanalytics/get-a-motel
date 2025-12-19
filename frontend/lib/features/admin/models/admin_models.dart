class AdminProperty {
  final String id;
  final String name;
  final String? description;
  final String addressLine1;
  final String city;
  final String state;
  final String zipCode;

  AdminProperty({
    required this.id,
    required this.name,
    this.description,
    required this.addressLine1,
    required this.city,
    required this.state,
    required this.zipCode,
  });

  factory AdminProperty.fromJson(Map<String, dynamic> json) => AdminProperty(
    id: json['id'],
    name: json['name'],
    description: json['description'],
    addressLine1: json['addressLine1'],
    city: json['city'],
    state: json['state'],
    zipCode: json['zipCode'],
  );

  Map<String, dynamic> toJson() => {
    'name': name,
    'description': description,
    'addressLine1': addressLine1,
    'city': city,
    'state': state,
    'zipCode': zipCode,
  };
}

class AdminRoomType {
  final String id;
  final String name;
  final String? description;
  final String? bedType;
  final int maxOccupancy;
  final int totalRooms;

  AdminRoomType({
    required this.id,
    required this.name,
    this.description,
    this.bedType,
    required this.maxOccupancy,
    required this.totalRooms,
  });

  factory AdminRoomType.fromJson(Map<String, dynamic> json) => AdminRoomType(
    id: json['id'],
    name: json['name'],
    description: json['description'],
    bedType: json['bedType'],
    maxOccupancy: json['maxOccupancy'],
    totalRooms: json['totalRooms'],
  );
}
