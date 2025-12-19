import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/network/dio_client.dart';
import '../models/room_type.dart';

class AvailabilityScreen extends ConsumerStatefulWidget {
  final String propertyId;
  const AvailabilityScreen({super.key, required this.propertyId});

  @override
  ConsumerState<AvailabilityScreen> createState() => _AvailabilityScreenState();
}

class _AvailabilityScreenState extends ConsumerState<AvailabilityScreen> {
  bool _isLoading = true;
  List<RoomType>? _roomTypes;

  @override
  void initState() {
    super.initState();
    _fetchRooms();
  }

  Future<void> _fetchRooms() async {
    try {
      final dio = ref.read(dioProvider);
      // In MVP, we fetch room types for property. Backend API needs to support this under public or specific endpoint.
      // For now, using as placeholder toward admin/properties/{id} but scoped correctly.
      final response = await dio.get('/admin/properties/${widget.propertyId}'); // Temp path
      // Note: Real implementation would use guest availability path.
      
      setState(() {
        _roomTypes = []; // Placeholder list
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Select a Room')),
      body: _isLoading 
          ? const Center(child: CircularProgressIndicator())
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: 5, // Demo count
              itemBuilder: (context, index) {
                return Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    title: Text(index == 0 ? 'Standard King Room' : 'Deluxe Queen Room'),
                    subtitle: const Text('Max occupants: 2 | Free WiFi'),
                    trailing: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Text('\$89.00', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.indigo)),
                        const SizedBox(height: 4),
                        ElevatedButton(
                          onPressed: () => context.push('/booking/room_id_$index'),
                          child: const Text('Book'),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }
}
