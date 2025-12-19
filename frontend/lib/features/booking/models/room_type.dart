class RoomType {
  final String id;
  final String name;
  final String? description;
  final String? bedType;
  final int maxOccupancy;
  final int basePriceCents;
  final int totalRooms;

  RoomType({
    required this.id,
    required this.name,
    this.description,
    this.bedType,
    required this.maxOccupancy,
    required this.basePriceCents,
    required this.totalRooms,
  });

  factory RoomType.fromJson(Map<String, dynamic> json) => RoomType(
    id: json['id'],
    name: json['name'],
    description: json['description'],
    bedType: json['bedType'],
    maxOccupancy: json['maxOccupancy'],
    basePriceCents: json['basePriceCents'],
    totalRooms: json['totalRooms'],
  );
}
