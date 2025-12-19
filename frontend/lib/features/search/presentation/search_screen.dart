import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../repositories/search_repository.dart';
import '../models/property.dart';
import 'package:intl/intl.dart';

class SearchScreen extends ConsumerStatefulWidget {
  const SearchScreen({super.key});

  @override
  ConsumerState<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends ConsumerState<SearchScreen> {
  final _cityController = TextEditingController();
  String? _selectedState = 'TX';
  DateTimeRange _dateRange = DateTimeRange(
    start: DateTime.now().add(const Duration(days: 1)),
    end: DateTime.now().add(const Duration(days: 2)),
  );
  int _guests = 1;
  List<PropertySearchResult>? _results;
  bool _isLoading = false;

  final List<String> _usStates = ['TX', 'CA', 'FL', 'NY', 'NV', 'AZ']; // MVP limited

  Future<void> _performSearch() async {
    setState(() => _isLoading = true);
    try {
      final results = await ref.read(searchRepositoryProvider).search(
        city: _cityController.text.isEmpty ? null : _cityController.text,
        state: _selectedState,
        checkIn: _dateRange.start,
        checkOut: _dateRange.end,
        guests: _guests,
      );
      setState(() {
        _results = results;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Search failed: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Find a Motel'),
        backgroundColor: Colors.white,
        elevation: 0,
        foregroundColor: Colors.indigo,
      ),
      body: Column(
        children: [
          _buildSearchHeader(),
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : _results == null
                    ? _buildEmptyState()
                    : _buildResultsList(),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchHeader() {
    return Container(
      padding: const EdgeInsets.all(16),
      color: Colors.indigo.shade50,
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                flex: 2,
                child: TextField(
                  controller: _cityController,
                  decoration: const InputDecoration(
                    labelText: 'City',
                    prefixIcon: Icon(Icons.location_city),
                    fillColor: Colors.white,
                    filled: true,
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: DropdownButtonFormField<String>(
                  value: _selectedState,
                  items: _usStates.map((s) => DropdownMenuItem(value: s, child: Text(s))).toList(),
                  onChanged: (v) => setState(() => _selectedState = v),
                  decoration: const InputDecoration(
                    labelText: 'State',
                    fillColor: Colors.white,
                    filled: true,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          InkWell(
            onTap: () async {
              final picked = await showDateRangePicker(
                context: context,
                firstDate: DateTime.now(),
                lastDate: DateTime.now().add(const Duration(days: 365)),
                initialDateRange: _dateRange,
              );
              if (picked != null) setState(() => _dateRange = picked);
            },
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
              decoration: BoxDecoration(
                color: Colors.white,
                border: Border.all(color: Colors.grey.shade400),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Row(
                children: [
                  const Icon(Icons.calendar_today, color: Colors.indigo),
                  const SizedBox(width: 12),
                  Text(
                    '${DateFormat('MMM d').format(_dateRange.start)} - ${DateFormat('MMM d').format(_dateRange.end)}',
                    style: const TextStyle(fontSize: 16),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          ElevatedButton(
            onPressed: _performSearch,
            style: ElevatedButton.styleFrom(
              minimumSize: const Size.fromHeight(50),
              backgroundColor: Colors.indigo,
              foregroundColor: Colors.white,
            ),
            child: const Text('Search Availability'),
          ),
        ],
      ),
    );
  }

  Widget _buildResultsList() {
    if (_results!.isEmpty) return const Center(child: Text('No motels found for these dates.'));
    
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _results!.length,
      itemBuilder: (context, index) {
        final result = _results![index];
        return Card(
          margin: const EdgeInsets.only(bottom: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Container(
                height: 150,
                color: Colors.grey.shade200,
                child: const Icon(Icons.hotel, size: 64, color: Colors.indigo),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      result.property.name,
                      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                    ),
                    Text('${result.property.city}, ${result.property.state}', style: const TextStyle(color: Colors.grey)),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          '\$${(result.lowestPriceCents / 100).toStringAsFixed(2)} / night',
                          style: const TextStyle(fontSize: 18, color: Colors.indigo, fontWeight: FontWeight.bold),
                        ),
                        ElevatedButton(
                          onPressed: () => context.push('/search/availability/${result.property.id}'),
                          child: const Text('View Rooms'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildEmptyState() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.search, size: 80, color: Colors.grey),
          SizedBox(height: 16),
          Text('Enter location and dates to find a room.', style: TextStyle(color: Colors.grey)),
        ],
      ),
    );
  }
}
